package com.bgeradz.multiplexer;

import java.io.IOException;

public class IOTrackerAdapter implements IOTracker {

	@Override
	public void beforeRead(TrackedInputStream inputStream, byte[] buffer, int offset, int length) {}

	@Override
	public void afterRead(TrackedInputStream inputStream, int bytesRead, byte[] buffer, int offset, int length) {}

	@Override
	public void afterReadException(TrackedInputStream inputStream, IOException exception, byte[] buffer, int offset, int length) {}

	@Override
	public void beforeWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length) {}

	@Override
	public void afterWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length) {}

	@Override
	public void afterWriteException(TrackedOutputStream outputStream, IOException exception, byte[] buffer, int offset, int length) {}

	@Override
	public void onClose(TrackedInputStream inputStream, Throwable cause) {}

	@Override
	public void onClose(TrackedOutputStream outputStream, Throwable cause) {}

}
