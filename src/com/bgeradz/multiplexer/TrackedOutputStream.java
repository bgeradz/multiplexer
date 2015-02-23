package com.bgeradz.multiplexer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CopyOnWriteArrayList;

public class TrackedOutputStream extends OutputStream {
	protected OutputStream wrapee;
	
	protected CopyOnWriteArrayList<IOTracker> trackers = new CopyOnWriteArrayList<IOTracker>();

	private boolean hasError;
	private boolean isClosed;
	
	public TrackedOutputStream(OutputStream wrapee) {
		this.wrapee = wrapee;
	}
	
	public synchronized void addTracker(IOTracker tracker) {
		if (! trackers.contains(tracker)) {
			trackers.add(tracker);
		}
	}
	public synchronized void removeTracker(IOTracker tracker) {
		trackers.remove(tracker);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for (IOTracker tracker : trackers) {
			tracker.beforeWrite(this, b, off, len);
		}
		try {
			if (hasError) {
				throw new IOException("hasError");
			}
			if (isClosed) {
				throw new IOException("isClosed");
			}
			wrapee.write(b, off, len);
			for (IOTracker tracker : trackers) {
				tracker.afterWrite(this, b, off, len);
			}
		} catch (IOException e) {
			hasError = true;
			for (IOTracker tracker : trackers) {
				tracker.afterWriteException(this, e, b, off, len);
			}
			throw e;
		}
	}
	
	@Override
	public void close() throws IOException {
		for (IOTracker tracker : trackers) {
			tracker.beforeClose(this);
		}
		try {
			isClosed = true;
			if (wrapee != null) {
				wrapee.close();
			}
			for (IOTracker tracker : trackers) {
				tracker.afterClose(this);
			}
		} catch (IOException e) {
			for (IOTracker tracker : trackers) {
				tracker.afterCloseException(this, e);
			}
			throw e;
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}	

	@Override
	public void write(int b) throws IOException {
		wrapee.write(b);
	}


	@Override
	public void flush() throws IOException {
		wrapee.flush();
	}

	
}
