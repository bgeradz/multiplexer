package com.bgeradz.multiplexer;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

public class HttpResponse implements Closeable {
	private static final Logger L = App.createLogger(HttpResponse.class.getSimpleName());
	private static final int BLOCK_SIZE = 1024 * 4;
	
	private HttpRequest request;
	private TrackedInputStream input;
	private TrackedOutputStream output;
	
	private String mimeType = "application/octet-stream";
	
	private int status = 200;
	private String message = "OK";
	
	private static HashMap<String, String> mimeTypeMap = new HashMap<String, String>();
	static {
		mimeTypeMap.put("mpeg", "video/mpeg");
		mimeTypeMap.put("mpg", "video/mpeg");
		mimeTypeMap.put("mpga", "audio/mpeg");
		mimeTypeMap.put("flv", "video/x-flv");
		mimeTypeMap.put("html", "text/html");
	}
	
	public HttpResponse(HttpRequest request, TrackedInputStream input) {
		this.request = request;
		this.input = input;
		this.output = request.getOutputStream();
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
		inferMimeType(request.getPath());
	}
	
	public HttpResponse(HttpRequest request, int status, String message) {
		this.request = request;
		this.message = message;
		this.status = status;
		this.input = new TrackedInputStream(new ByteArrayInputStream(message.getBytes()), "status");
		this.output = request.getOutputStream();
		
		inferMimeType(request.getPath());
	}
	
	public void transfer() throws IOException {
		try {
			writeHeaders();
			byte[] block = new byte[BLOCK_SIZE];
			while (true) {
				int bytesRead = input.read(block);
				if (bytesRead > 0) {
					output.write(block, 0, bytesRead);
				} else {
					break;
				}
			}
		} finally {
			close();
		}
	}
	
	@Override
	public void close() {
		Util.close(request);
		Util.close(input);
		Util.close(output);
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	private void writeHeaders() throws IOException {
		output.write(("HTTP/1.0 "+ status +" "+ message +"\r\n").getBytes());
		output.write(("Content-type: "+ mimeType +"\r\n").getBytes());
		output.write("Connection: close\r\n".getBytes());
		output.write("Cache-Control: no-cache\r\n".getBytes());
		output.write("\r\n".getBytes());
		output.flush();
	}
	
	private void inferMimeType(String path) {
		int dotPos = path.lastIndexOf(".");
		if (dotPos > 0) {
			String ext = path.substring(dotPos + 1);
			String mimeType = mimeTypeMap.get(ext);
			if (mimeType != null) {
				L.info("Inferred mime type from extension "+ ext + " => "+ mimeType);
				this.mimeType = mimeType;
			}
		}
		if (mimeType == null) {
			L.info("Could not infer mime type from path "+ path);
		}
	}
}
