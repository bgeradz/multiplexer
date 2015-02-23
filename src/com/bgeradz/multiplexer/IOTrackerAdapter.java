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
	public void beforeClose(TrackedInputStream inputStream) {}

	@Override
	public void afterClose(TrackedInputStream inputStream) {}

	@Override
	public void afterCloseException(TrackedInputStream inputStream, IOException e) {}

	@Override
	public void beforeWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length) {}

	@Override
	public void afterWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length) {}

	@Override
	public void afterWriteException(TrackedOutputStream outputStream, IOException exception, byte[] buffer, int offset, int length) {}

	@Override
	public void beforeClose(TrackedOutputStream inputStream) {}

	@Override
	public void afterClose(TrackedOutputStream inputStream) {}

	@Override
	public void afterCloseException(TrackedOutputStream inputStream, IOException e) {}
}
