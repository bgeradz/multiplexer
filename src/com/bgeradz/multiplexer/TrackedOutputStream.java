package com.bgeradz.multiplexer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CopyOnWriteArrayList;

public class TrackedOutputStream extends OutputStream {
	private static final Logger L = App.createLogger(TrackedOutputStream.class.getSimpleName());
	protected OutputStream wrapee;
	
	protected CopyOnWriteArrayList<IOTracker> trackers = new CopyOnWriteArrayList<IOTracker>();

	protected String name;
	protected boolean isClosed;
	
	protected long lastWriteTime;
	
	public TrackedOutputStream(OutputStream wrapee, String name) {
		this.wrapee = wrapee;
		this.name = name;
		App.addTrackedOutputStream(this);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getLastWriteTime() {
		return lastWriteTime;
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
			if (isClosed) {
				throw new IOException("isClosed");
			}
			wrapee.write(b, off, len);
			lastWriteTime = System.currentTimeMillis();
			for (IOTracker tracker : trackers) {
				tracker.afterWrite(this, b, off, len);
			}
		} catch (IOException e) {
			close(e);
			throw e;
		}
	}
	
	@Override
	public void close() {
		close(null);
	}
	
	protected void close(IOException cause) {
		boolean closed = false;
		synchronized (this) {
			if (! isClosed) {
				closed = true;
				App.removeTrackedOutputStream(this);
				isClosed = true;
			}
		}		
		if (closed) {
			Util.close(wrapee);
			for (IOTracker tracker : trackers) {
				tracker.onClose(this, cause);
			}
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}	

	@Override
	public void write(int b) throws IOException {
		// TODO: not tracked
		wrapee.write(b);
	}


	@Override
	public void flush() throws IOException {
		wrapee.flush();
	}

	
}
