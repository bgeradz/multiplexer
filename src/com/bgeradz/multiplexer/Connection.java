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

	private TrackedInputStream input;
	private TrackedOutputStream output;
	
	private boolean inputClosed;
	private boolean outputClosed;
	
	private boolean autoCloseInput = true;
	private boolean autoCloseOutput = true;

    private SpeedMeasurer speedMeasurer;

	private State state;
	ConnectionIOTracker tracker;

	public Connection(TrackedInputStream input, TrackedOutputStream output) {
		this.input = input;
		this.output = output;
		tracker = new ConnectionIOTracker();
		input.addTracker(tracker);
		output.addTracker(tracker);
		App.addConnection(this);
		state = State.IDLE;
	}

    public void setSpeedMeasurer(SpeedMeasurer speedMeasurer) {
        this.speedMeasurer = speedMeasurer;
    }

	public TrackedInputStream getInput() {
		return input;
	}
	
	public TrackedOutputStream getOutput() {
		return output;
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

    public double getAverageSpeed() {
        if (speedMeasurer == null) {
            return 0.0;
        } else {
            return speedMeasurer.getAverageSpeed();
        }
    }

	public Connection autoCloseInput(boolean value) {
		this.autoCloseInput = value;
		return this;
	}
	public Connection autoCloseOutput(boolean value) {
		this.autoCloseOutput = value;
		return this;
	}
	
 	private void switchState(State state) {
		this.state = state;
		lastStateChangeTime = System.currentTimeMillis();
	}
	
	private void checkClosed() {
		if ((inputClosed || ! autoCloseInput) && (outputClosed || ! autoCloseOutput)) {
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
            if (speedMeasurer != null) {
                speedMeasurer.report(length);
            }
		}

		@Override
		public void afterWriteException(TrackedOutputStream outputStream, IOException exception, byte[] buffer, int offset, int length) {
			switchState(State.IDLE);
		}

		@Override
		public void onClose(TrackedInputStream inputStream, Throwable cause) {
			inputClosed = true;
			if (autoCloseOutput) {
				output.close();
			}
			checkClosed();
		}
		
		@Override
		public void onClose(TrackedOutputStream outputStream, Throwable cause) {
			outputClosed = true;
			if (autoCloseInput) {
				input.close();
			}
			checkClosed();
		}
	}
}
