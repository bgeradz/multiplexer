package com.bgeradz.multiplexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CopyOnWriteArrayList;

public class TrackedInputStream extends InputStream {
	private InputStream wrapee;
	
	private CopyOnWriteArrayList<IOTracker> trackers = new CopyOnWriteArrayList<IOTracker>();
	
	private String name;
	private boolean isClosed;

	public TrackedInputStream(InputStream wrapee, String name) {
		this.wrapee = wrapee;
		this.name = name;
		App.addTrackedInputStream(this);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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
	public int read(byte[] b, int off, int len) throws IOException {
		for (IOTracker tracker : trackers) {
			tracker.beforeRead(this, b, off, len);
		}
		try {
			if (isClosed) {
				throw new IOException("isClosed");
			}
			int ret = wrapee.read(b, off, len);
			for (IOTracker tracker : trackers) {
				tracker.afterRead(this, ret, b, off, len);
			}
			return ret;
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
				App.removeTrackedInputStream(this);
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
	public int read() throws IOException {
		return 0;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public long skip(long n) throws IOException {
		return wrapee.skip(n);
	}

	@Override
	public int available() throws IOException {
		return wrapee.available();
	}

	@Override
	public void mark(int readlimit) {
		wrapee.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		wrapee.reset();
	}

	@Override
	public boolean markSupported() {
		return wrapee.markSupported();
	}
	
}
