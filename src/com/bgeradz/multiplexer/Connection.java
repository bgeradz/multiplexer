package com.bgeradz.multiplexer;

import java.io.IOException;

public class Connection {
	public enum State {
		IDLE,
		READ,
		WRITE
	}	
	
	private long lastStateChangeTime;
	@SuppressWarnings("unused")
	private long totalBytesRead;
	private long totalBytesWritten;

	private String name;
	
	private TrackedInputStream input;
	private TrackedOutputStream output;
	
	private boolean inputClosed;
	private boolean outputClosed;
	
	private State state;
	ConnectionIOTracker tracker;

	public Connection(String name, TrackedInputStream input, TrackedOutputStream output) {
		this.name = name;
		this.input = input;
		this.output = output;
		tracker = new ConnectionIOTracker();
		input.addTracker(tracker);
		output.addTracker(tracker);
		App.addConnection(this);
		state = State.IDLE;
	}
	
	public String getName() {
		return name;
	}
		
	public long getBytesTransferred() {
		return totalBytesWritten;
	}

	public State getState() {
		return state;
	}
	
	public long getLastStateChangeTime() {
		return lastStateChangeTime;
	}


	private void switchState(State state) {
		this.state = state;
		lastStateChangeTime = System.currentTimeMillis();
	}
	
	private void checkClosed() {
		if (inputClosed && outputClosed) {
			App.removeConnection(this);
		}
	}
	
	private class ConnectionIOTracker extends IOTrackerAdapter {	
		@Override
		public void beforeRead(TrackedInputStream inputStream, byte[] buffer, int offset, int length) {
			switchState(State.READ);
		}

		@Override
		public void afterRead(TrackedInputStream inputStream, int bytesRead, byte[] buffer, int offset, int length) {
			switchState(State.IDLE);
			totalBytesRead += bytesRead;
		}

		@Override
		public void afterReadException(TrackedInputStream inputStream, IOException exception, byte[] buffer, int offset, int length) {
			switchState(State.IDLE);
		}

		
		@Override
		public void beforeWrite(TrackedOutputStream outputStream, byte[] buffer, int offset, int length) {
			switchState(State.WRITE);
		}

		@Override
		public void afterWrite(TrackedOutputStream outputStream, byte[] buffer,	int offset, int length) {
			switchState(State.IDLE);
			totalBytesWritten += length;
		}

		@Override
		public void afterWriteException(TrackedOutputStream outputStream, IOException exception, byte[] buffer, int offset, int length) {
			switchState(State.IDLE);
		}

		@Override
		public void onClose(TrackedInputStream inputStream, IOException cause) {
			inputClosed = true;
			output.close();
			checkClosed();
		}
		
		@Override
		public void onClose(TrackedOutputStream outputStream, IOException cause) {
			outputClosed = true;
			input.close();
			checkClosed();
		}
	}
}
