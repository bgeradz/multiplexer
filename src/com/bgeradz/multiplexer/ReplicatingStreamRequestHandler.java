package com.bgeradz.multiplexer;

import java.io.IOException;

public class ReplicatingStreamRequestHandler implements HttpRequestHandler {
	private final Logger L;
	
	private static final int BUFFER_SIZE = 256 * 1024;
	private static final int BLOCK_SIZE = 4 * 1024;
	
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
			replicatingOutput = new ReplicatingOutputStream();
			replicatingOutput.addTracker(new IOTrackerAdapter() {
				@Override
				public void onClose(TrackedOutputStream inputStream, IOException cause) {
					synchronized (ReplicatingStreamRequestHandler.this) {
						stopThread();
					}
				}
			});
			
			try {
				input = dataSource.open();
				input.addTracker(new IOTrackerAdapter() {
					@Override
					public void onClose(TrackedInputStream inputStream, IOException cause) {
						replicatingOutput.close();
					}
				});
				id = App.getUniqueId();
				new Connection("REP["+ id +"] <= "+ name, input, replicatingOutput);			
			} catch (IOException e) {
				Util.close(request.getOutputStream());
				throw e;
			}
			thread = new TransferThread();
			thread.start();
		}
		
		CircularByteBuffer buffer = new CircularByteBuffer(BUFFER_SIZE, false);
		final TrackedOutputStream bufferOutputStream = new TrackedOutputStream(buffer.getOutputStream());
		final TrackedInputStream bufferInputStream = new TrackedInputStream(buffer.getInputStream());
		
		replicatingOutput.addOutputStream(bufferOutputStream);
		
		String resource = "("+ request.getName() +" "+ request.getPath() +")";
		String connectionName = "REP["+ id +"] >= buffer for " + resource;		
		new Connection(connectionName, input, bufferOutputStream).autoCloseInput(false);
		
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
							shouldStop = false;
							break;
						}
					}
					int bytesRead = input.read(block);
					replicatingOutput.write(block, 0, bytesRead);
				}
			} catch (IOException e) {
				L.warn("Input exception: "+ e);
			} finally {
				Util.close(input);
				Util.close(replicatingOutput);
				synchronized (this) {
					thread = null;
				}
				L.info("Thread terminated");
			}
		}
	}
}
