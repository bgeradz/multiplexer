package com.bgeradz.multiplexer;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReplicatingOutputStream extends TrackedOutputStream {
	private static final Logger L = App.createLogger(ReplicatingOutputStream.class.getSimpleName());
	
	private CopyOnWriteArrayList<TrackedOutputStream> outputStreams = new CopyOnWriteArrayList<TrackedOutputStream>();
	
	public ReplicatingOutputStream() {
		super(null);
		addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedInputStream inputStream,	IOException cause) {
				closeOutputs();
			}
		});
	}
	
	private void closeOutputs() {
		for (TrackedOutputStream output : outputStreams) {
			output.close();
		}
	}
	
	public synchronized void addOutputStream(final TrackedOutputStream output) throws IOException {
		output.addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedOutputStream outputStream, IOException cause) {
				removeOutputStream(output);
			}
		});
		outputStreams.add(output);
		L.info("Output added ("+ outputStreams.size() +")");
	}
	
	public synchronized void removeOutputStream(TrackedOutputStream output) {
		if (outputStreams.contains(output)) {
			outputStreams.remove(output);
			L.info("Output removed ("+ outputStreams.size() +")");
			if (outputStreams.size() == 0) {
				close();
			}
		}
	}

	
	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for (IOTracker tracker : trackers) {
			tracker.beforeWrite(this, b, off, len);
		}
		try {
			if (isClosed) {
				throw new IOException("isClosed");
			}
			
			for (TrackedOutputStream output : outputStreams) {
				try {
					output.write(b, off, len);
				} catch (IOException e) {
					Util.close(output);
				}
			}
			
			for (IOTracker tracker : trackers) {
				tracker.afterWrite(this, b, off, len);
			}
		} catch (IOException e) {
			close(e);
			throw e;
		}
	}

	@Override
	public void flush() throws IOException {
		if (isClosed) {
			throw new IOException("closed");
		}
		for (TrackedOutputStream output : outputStreams) {
			try {
				output.flush();
			} catch (IOException e) {
				Util.close(output);
			}
		}
	}
}
