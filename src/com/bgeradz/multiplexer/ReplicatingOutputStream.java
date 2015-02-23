package com.bgeradz.multiplexer;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReplicatingOutputStream extends TrackedOutputStream {
	private static final Logger L = App.createLogger(ReplicatingOutputStream.class.getSimpleName());
	
	private CopyOnWriteArrayList<TrackedOutputStream> outputStreams = new CopyOnWriteArrayList<TrackedOutputStream>();
	
	private boolean isClosed;
	
	public ReplicatingOutputStream() {
		super(null);
	}
	
	public synchronized void addOutputStream(final TrackedOutputStream output) throws IOException {
		output.addTracker(new IOTrackerAdapter() {
			@Override
			public void beforeClose(TrackedOutputStream outputStream) {
				if (! isClosed) {
					removeOutputStream(output);
				}
			}
		});
		outputStreams.add(output);
		L.info("Output added ("+ outputStreams.size() +")");
	}
	
	public synchronized void removeOutputStream(TrackedOutputStream output) {
		if (outputStreams.contains(output)) {
			outputStreams.remove(output);
			L.info("Output removed ("+ outputStreams.size() +")");
		}
		if (outputStreams.size() == 0) {
			Util.close(this);
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
		if (isClosed) {
			throw new IOException("closed");
		}
		
		for (IOTracker tracker : trackers) {
			tracker.beforeWrite(this, b, off, len);
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

	@Override
	public void close() throws IOException {
		if (! isClosed) {
			isClosed = true;
			for (TrackedOutputStream output : outputStreams) {
				Util.close(output);
			}
		}
		super.close();
	}
	
}
