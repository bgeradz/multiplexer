package com.bgeradz.multiplexer;

import java.io.IOException;

public class ReplicatingStreamRequestHandler implements HttpRequestHandler {
	private final Logger L;
	
	private final int clientBufferSize;
	private final int blockSize;
	// Close when this much time has passed with no active reading clients.
	private final long autoCloseDelay;
	
	private final DataSource dataSource;
	private TrackedInputStream input;
	private ReplicatingOutputStream replicatingOutput;
	
	private String name;
	
	private Thread thread;
	
	private boolean shouldStop;
	
	private int id;

	public ReplicatingStreamRequestHandler(Config config) {
		L = App.createLogger(ReplicatingStreamRequestHandler.class.getSimpleName() + "[" + name +"]");
		this.name = config.getName();
		this.dataSource = config.getDataSorce().build();
        this.clientBufferSize = config.getClientBufferSize();
        this.blockSize = config.getBlockSize();
        this.autoCloseDelay = config.getAutoCloseDelay();
	}

	@Override
	public synchronized HttpResponse getResponse(HttpRequest request) throws IOException {
		if (thread == null) {
			shouldStop = false;
			try {
				id = App.getUniqueId();

				input = dataSource.open();
				replicatingOutput = new ReplicatingOutputStream("REP["+ id +"] ("+ name +")", autoCloseDelay);
				replicatingOutput.addTracker(new IOTrackerAdapter() {
					@Override
					public void onClose(TrackedOutputStream inputStream, Throwable cause) {
						synchronized (ReplicatingStreamRequestHandler.this) {
							stopThread();
						}
					}
				});

				input.addTracker(new IOTrackerAdapter() {
					@Override
					public void onClose(TrackedInputStream inputStream, Throwable cause) {
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
		
		CircularByteBuffer buffer = new CircularByteBuffer(clientBufferSize, false);
		String bufferName = "REP["+ id +"]:buffer["+ App.getUniqueId() +"] ("+ name +")";
		final TrackedOutputStream bufferOutputStream = new TrackedOutputStream(buffer.getOutputStream(), bufferName);
		final TrackedInputStream bufferInputStream = new TrackedInputStream(buffer.getInputStream(), bufferName);
		
		replicatingOutput.addOutputStream(bufferOutputStream);
		
		new Connection(input, bufferOutputStream).autoCloseInput(false);
		new Connection(bufferInputStream, request.getOutputStream());
		
		bufferInputStream.addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedInputStream inputStream, Throwable cause) {
				bufferOutputStream.close();
			}
		});
		bufferOutputStream.addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedOutputStream outputStream, Throwable cause) {
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
				byte[] block = new byte[blockSize];
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

    public static class Config implements Configurator<ReplicatingStreamRequestHandler> {
        private String name;
        private Configurator<DataSource> dataSource;
        private int clientBufferSize = 256 * 1024;
        private int blockSize = 4 * 1024;
        private long autoCloseDelay = 60000;

        public int getClientBufferSize() {
            return clientBufferSize;
        }

        public void setClientBufferSize(int clientBufferSize) {
            this.clientBufferSize = clientBufferSize;
        }

        public int getBlockSize() {
            return blockSize;
        }

        public void setBlockSize(int blockSize) {
            this.blockSize = blockSize;
        }

        public long getAutoCloseDelay() {
            return autoCloseDelay;
        }

        public void setAutoCloseDelay(long autoCloseDelay) {
            this.autoCloseDelay = autoCloseDelay;
        }

        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

        public void setDataSource(Configurator<DataSource> dataSource) {
            this.dataSource = dataSource;
        }

        public Configurator<DataSource> getDataSorce() {
            return dataSource;
        }

        @Override
        public void validate() throws ConfigException {
            if (name == null) {
                throw new ConfigException("name unspecified");
            }
            if (dataSource == null) {
                throw new ConfigException("dataSource unspecified");
            }
        }

        @Override
        public ReplicatingStreamRequestHandler build() {
            return new ReplicatingStreamRequestHandler(this);
        }
    }
}
