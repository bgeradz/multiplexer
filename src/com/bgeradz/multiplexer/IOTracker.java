package com.bgeradz.multiplexer;

import java.io.IOException;

public interface IOTracker {
	void beforeRead(TrackedInputStream inputStream, byte[] buffer, int offset, int length);
	void afterRead(TrackedInputStream inputStream, int bytesRead, byte[] buffer, int offset, int length);
	void afterReadException(TrackedInputStream inputStream, IOException exception, byte[] buffer, int offset, int length);
	
	void beforeWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length);
	void afterWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length);
	void afterWriteException(TrackedOutputStream outputStream, IOException exception, byte[] buffer, int offset, int length);
	
	void onClose(TrackedInputStream inputStream, IOException cause);
	void onClose(TrackedOutputStream outputStream, IOException cause);
}
