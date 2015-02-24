package com.bgeradz.multiplexer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ProcessOutputDataSource implements DataSource {
	private final Logger L;
	
	private String command;
	
	public ProcessOutputDataSource(String command) {
		L = App.createLogger(getClass().getSimpleName() + "[" + command + "]");
		this.command = command;
	}
	
	@Override
	public TrackedInputStream open() throws IOException {
		return new Handler().handle();
	}

	private class Handler {
		private Process process;
		private TrackedInputStream input;
		
		public TrackedInputStream handle() throws IOException {
			L.info("Starting process");
			Process process = Runtime.getRuntime().exec(command);
			input = new TrackedInputStream(process.getInputStream(), command);
			this.process = process;
			
			input.addTracker(new IOTrackerAdapter() {
				@Override
				public void onClose(TrackedInputStream inputStream, IOException cause) {
					L.info("Input closed, cause: "+ cause);
					shutdown();
				}
			});
			return input;
		}

		private synchronized void shutdown() {
			if (process != null) {
				L.info("Shutting down");
				Util.close(input);
				L.info("waiting for process...");
				try {
					process.waitFor(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {}
	
				L.info("destroying process");
				process.destroy();
				process = null;
			}
		}
	}
}
