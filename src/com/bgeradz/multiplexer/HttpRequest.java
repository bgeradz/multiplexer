package com.bgeradz.multiplexer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest implements Closeable {
	private final Logger L;
	
	private Socket socket;
	private TrackedInputStream input;
	private TrackedOutputStream output;
	private String name;
	
	private String path;
	private HashMap<String, String> params = new HashMap<String, String>();

	public HttpRequest(Socket socket) throws IOException {
		this.socket = socket;
		name = socket.getInetAddress().toString() + ":" + socket.getPort();
		L = App.createLogger(getClass().getSimpleName() + "[" + name + "]");

		input = new TrackedInputStream(socket.getInputStream());
		output = new TrackedOutputStream(socket.getOutputStream());
		input.addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedInputStream inputStream,	IOException cause) {
				close();
			}
		});
		output.addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedOutputStream outputStream, IOException cause) {
				close();
			}			
		});
	}
	
	public String getName() {
		return name;
	}
	
	public void readHeaders() throws IOException {
		try {
			doReadHeaders();
		} catch (IOException e) {
			close();
			throw e;
		}
	}
	
	public String getPath() {
		return path;
	}
	
	public String getParam(String name) {
		return params.get(name);
	}
	
	public Map<String, String> getParams() {
		return params;
	}
	
	public TrackedOutputStream getOutputStream() {
		return output;
	}

	public void doReadHeaders() throws IOException {
		Reader reader = new InputStreamReader(input);
		
		String requestLine = readLine(reader, -1);
		if (requestLine == null) {
			throw new IOException("requestLine is null");
		}
		String[] arr = requestLine.split(" ");
		String uri = arr[1];
		L.info("URI requested: " + uri);
		
		arr = uri.split("\\?");
		path = arr[0];
		if (arr.length > 1) {
			arr = arr[1].split("\\&");
			for (String pairString : arr) {
				String[] pair = pairString.split("=");
				String name = URLDecoder.decode(pair[0], "utf-8");
				String value = URLDecoder.decode(pair[1], "utf-8");
				params.put(name, value);
			}
		}
		
		while (true) {
			String header = readLine(reader, -1);
			if (header == null) {
				throw new IOException("Null header");
			}
			// log("[HTTP] "+ header);
			int colonPos = header.indexOf(":");
			if (colonPos > 0) {
				String name = header.substring(0, colonPos);
				String value = header.substring(colonPos + 1).trim();
				// L.debug("Request header "+ name + ": " + value);
			}
			if (header.trim().length() == 0) {
				break;
			}
		}
	}
	
	@Override
	public void close() {
		Util.close(input);
		Util.close(output);
		if (socket != null) {
			Util.close(socket);
			socket = null;
		}
	}

	private String readLine(Reader reader, int maxLength) throws IOException {
		StringBuilder buf = new StringBuilder();
		while (true) {
			int c = reader.read();
			if (c == -1) {
				if (buf.length() == 0) {
					return null;
				}
				break;
			} else if (c == '\r') {
				continue;
			} else if (c == '\n') {
				break;
			} else {
				buf.append((char) c);
			}
			if (buf.length() == maxLength) {
				break;
			}
		}
		return buf.toString();
	}
}
