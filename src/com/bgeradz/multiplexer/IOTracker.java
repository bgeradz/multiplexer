package com.bgeradz.multiplexer;

import java.io.IOException;

public interface IOTracker {
	void beforeRead(TrackedInputStream inputStream, byte[] buffer, int offset, int length);
	void afterRead(TrackedInputStream inputStream, int bytesRead, byte[] buffer, int offset, int length);
	void afterReadException(TrackedInputStream inputStream, IOException exception, byte[] buffer, int offset, int length);
	
	void beforeClose(TrackedInputStream inputStream);
	void afterClose(TrackedInputStream inputStream);
	void afterCloseException(TrackedInputStream inputStream, IOException e);
	
	void beforeWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length);
	void afterWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length);
	void afterWriteException(TrackedOutputStream outputStream, IOException exception, byte[] buffer, int offset, int length);
	
	void beforeClose(TrackedOutputStream inputStream);
	void afterClose(TrackedOutputStream inputStream);
	void afterCloseException(TrackedOutputStream inputStream, IOException e);
}
