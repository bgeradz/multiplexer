package com.bgeradz.multiplexer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Launcher {
	private static final Logger L = App.createLogger(Launcher.class.getSimpleName());

	private String[] args;
	private int argIndex = 0;
	private Coordinator coordinator = new Coordinator();
	
	public static void main(String[] args) {
		try {
			new Launcher(args);
		} catch (Exception e) {
			L.error("Exception during initialization", e);
		}
	}
	
	private Launcher(String[] args) {
		this.args = args;

		if (args.length < 5) {
			usage();
		}
		
		String addr = nextArg();
		String sport = nextArg();
		int port = 0;
		try {
			port = Integer.parseInt(sport);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid port: "+ sport +", argument "+ argIndex);
		}
		
		HttpRequestHandler handler = null;
		
		while (true) {
			String path = nextArg();
			if (path == null) {
				break;
			}
			
			boolean replicate = false;
			String dataSourceType = nextArg();
			if (dataSourceType == null) {
				throw new IllegalArgumentException("Expecting reuse option or data source type, argument "+ argIndex);
			}
			if (dataSourceType.equals("replicate")) {
				replicate = true;
				dataSourceType = nextArg();
				if (dataSourceType == null) {
					throw new IllegalArgumentException("Expecting data source type, argument "+ argIndex);
				}
			}
			
			if ("url".equals(dataSourceType)) {
				String surl = nextArg();
				if (surl == null) {
					throw new IllegalArgumentException("Expecting url, argument "+ argIndex);
				}
				URL url = null;
				try {
					url = new URL(surl);
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException("Invalid url "+ surl +", argument "+ argIndex);
				}
				DataSource dataSource = new UrlDataSource(url);
				handler = getStreamHandler(path, dataSource, replicate);
			} else if ("process".equals(dataSourceType)) {
				String command = nextArg();
				if (command == null) {
					throw new IllegalArgumentException("Expecting command, argument "+ argIndex);
				}
				DataSource dataSource = new ProcessOutputDataSource(command);
				handler = getStreamHandler(path, dataSource, replicate);
			} else if ("command".equals(dataSourceType)) {
				String command = nextArg();
				if (command == null) {
					throw new IllegalArgumentException("Expecting command, argument "+ argIndex);
				}
				String paramName = nextArg();
				handler = new CommandRequestHandler(command, paramName);
			} else if ("proxy".equals(dataSourceType)) {
				String urlBase = nextArg();
				if (urlBase == null) {
					throw new IllegalArgumentException("Expecting url-base, argument "+ argIndex);
				}
				handler = new ProxyRequestHandler(urlBase);
			} else if ("status".equals(dataSourceType)) {
				handler = new StatusRequestHandler();
			} else {
				throw new IllegalArgumentException("Invalid data source type: "+ dataSourceType +", argument "+ argIndex);
			}

			L.info(path + " => " + handler.getClass().getSimpleName());
			coordinator.addRequestHandler(path, handler);
		}
		
		HttpServer httpServer = new HttpServer(addr, port, coordinator);
		try {
			httpServer.start();
		} catch (IOException e) {
			L.error("Cannot start HTTP server", e);
		}
	}
	
	private HttpRequestHandler getStreamHandler(String handlerName, DataSource dataSource, boolean replicate) {
		if (replicate) {
			return new ReplicatingStreamRequestHandler(handlerName, dataSource);
		} else {
			return new SimpleStreamRequestHandler(handlerName, dataSource);
		}
	}
	
	private static void usage() {
		L.error("Usage: multiplexer <addr> <port>");
		L.error("    <path> [replicate] url <url>");
		L.error("  | <path> [replicate] process <command>");
		L.error("  | <path> command <command> <param-name>");
		L.error("  | <path> proxy <url>");
		L.error("  | <path> status");
		L.error("  ...");
		System.exit(1);
	}
	
	private String nextArg() {
		if (argIndex < args.length) {
			return args[argIndex++];
		} else {
			return null;
		}
	}
}
