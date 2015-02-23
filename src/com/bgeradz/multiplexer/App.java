package com.bgeradz.multiplexer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class App {
	private static CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<Connection>();

	private static final AtomicInteger curUniqueId = new AtomicInteger();
	
	private static final AtomicInteger trackedStreamCount = new AtomicInteger();
	
	public static Logger createLogger(String tag) {
		return new AppLogger(tag);
	}
	
	public static List<Connection> getConnections() {
		return connections;
	}
	
	public static synchronized void addConnection(Connection connection) {
		connections.add(connection);
	}
	public static synchronized void removeConnection(Connection connection) {
		connections.remove(connection);
	}
	
	public static int getUniqueId() {
		return curUniqueId.incrementAndGet();
	}
	
	public static void addTrackedInputStream(TrackedInputStream inputStream) {
		trackedStreamCount.incrementAndGet();
	}
	
	public static void addTrackedOutputStream(TrackedOutputStream outputStream) {
		trackedStreamCount.incrementAndGet();
	}
	
	public static void removeTrackedInputStream(TrackedInputStream inputStream) {
		trackedStreamCount.decrementAndGet();
	}
	
	public static void removeTrackedOutputStream(TrackedOutputStream outputStream) {
		trackedStreamCount.decrementAndGet();
	}
	
	public static int getTrackedStreamCount() {
		return trackedStreamCount.get();
	}
	
}
