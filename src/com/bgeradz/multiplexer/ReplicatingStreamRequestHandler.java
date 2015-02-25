package com.bgeradz.multiplexer;

import java.io.IOException;

public class ReplicatingStreamRequestHandler implements HttpRequestHandler {
	private final Logger L;
	
	private static final int BUFFER_SIZE = 256 * 1024;
	private static final int BLOCK_SIZE = 4 * 1024;
	
	// Close when this much time has passed with no active reading clients.
	// TODO: make configurable.
	private static final long AUTO_CLOSE_DELAY = 10000;
	
	private DataSource dataSource;
	private TrackedInputStream input;
	private ReplicatingOutputStream replicatingOutput;
	
	private String name;
	
	private Thread thread;
	
	private boolean shouldStop;
	
	private int id;

	public ReplicatingStreamRequestHandler(String name, DataSource dataSource) {
		L = App.createLogger(ReplicatingStreamRequestHandler.class.getSimpleName() + "[" + name +"]");
		this.name = name;
		this.dataSource = dataSource;
	}

	@Override
	public synchronized HttpResponse getResponse(HttpRequest request) throws IOException {
		if (thread == null) {
			shouldStop = false;
			try {
				id = App.getUniqueId();

				input = dataSource.open();
				replicatingOutput = new ReplicatingOutputStream("REP["+ id +"] ("+ name +")", AUTO_CLOSE_DELAY);
				replicatingOutput.addTracker(new IOTrackerAdapter() {
					@Override
					public void onClose(TrackedOutputStream inputStream, IOException cause) {
						synchronized (ReplicatingStreamRequestHandler.this) {
							stopThread();
						}
					}
				});

				input.addTracker(new IOTrackerAdapter() {
					@Override
					public void onClose(TrackedInputStream inputStream, IOException cause) {
						replicatingOutput.close();
					}
				});
				new Connection(input, replicatingOutput);
			} catch (IOException e) {
				Util.close(request.getOutputStream());
				throw e;
			}
			thread = new TransferThread();
			thread.start();
		}
		
		CircularByteBuffer buffer = new CircularByteBuffer(BUFFER_SIZE, false);
		String bufferName = "REP["+ id +"]:buffer["+ App.getUniqueId() +"] ("+ name +")";
		final TrackedOutputStream bufferOutputStream = new TrackedOutputStream(buffer.getOutputStream(), bufferName);
		final TrackedInputStream bufferInputStream = new TrackedInputStream(buffer.getInputStream(), bufferName);
		
		replicatingOutput.addOutputStream(bufferOutputStream);
		
		new Connection(input, bufferOutputStream).autoCloseInput(false);
		new Connection(bufferInputStream, request.getOutputStream());
		
		bufferInputStream.addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedInputStream inputStream, IOException cause) {
				bufferOutputStream.close();
			}
		});
		bufferOutputStream.addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedOutputStream outputStream, IOException cause) {
				bufferInputStream.close();
			}
		});
		
		HttpResponse response = new HttpResponse(request, bufferInputStream);
		return response;
	}
	
	private void stopThread() {
		synchronized (this) {
			if (thread != null) {
				shouldStop = true;
			}
		}
	}
	
	private class TransferThread extends Thread {
		@Override
		public void run() {
			L.info("Thread started");
			try {
				byte[] block = new byte[BLOCK_SIZE];
				while (true) {
					synchronized (this) {
						if (shouldStop) {
							L.info("shouldStop, breaking loop");
							break;
						}
					}
					int bytesRead = input.read(block);
					if (bytesRead <= 0) {
						break;
					}
					replicatingOutput.write(block, 0, bytesRead);
				}
			} catch (IOException e) {
				L.warn("Input exception: "+ e);
			} finally {
				input.close();
				replicatingOutput.close();
				synchronized (this) {
					thread = null;
					shouldStop = false;
				}
				L.info("Thread terminated");
			}
		}
	}
}
