package com.bgeradz.multiplexer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class App {
	private static CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<Connection>();

	private static final AtomicInteger curUniqueId = new AtomicInteger();

	private static HashSet<TrackedInputStream> trackedInputStreams = new HashSet<TrackedInputStream>();
	private static HashSet<TrackedOutputStream> trackedOutputStreams = new HashSet<TrackedOutputStream>();
	
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
	
	public static synchronized void addTrackedInputStream(TrackedInputStream inputStream) {
		trackedInputStreams.add(inputStream);
	}
	
	public static void addTrackedOutputStream(TrackedOutputStream outputStream) {
		trackedOutputStreams.add(outputStream);
	}
	
	public static void removeTrackedInputStream(TrackedInputStream inputStream) {
		trackedInputStreams.remove(inputStream);
	}
	
	public static void removeTrackedOutputStream(TrackedOutputStream outputStream) {
		trackedOutputStreams.remove(outputStream);
	}
	
	public static Set<TrackedInputStream> getTrackedInputStreams() {
		return trackedInputStreams;
	}
	
	public static Set<TrackedOutputStream> getTrackedOutputStreams() {
		return trackedOutputStreams;
	}
	
}
