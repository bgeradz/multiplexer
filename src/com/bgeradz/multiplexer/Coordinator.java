package com.bgeradz.multiplexer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Coordinator {
	@SuppressWarnings("unused")
	private static final Logger L = App.createLogger(Coordinator.class.getSimpleName());
	
	private ArrayList<String> paths = new ArrayList<String>();
	private ArrayList<HttpRequestHandler> requestHandlers = new ArrayList<HttpRequestHandler>();
	
	public void addRequestHandler(String path, HttpRequestHandler handler) {
		paths.add(path);
		requestHandlers.add(handler);		
	}
	
	public HttpResponse getResponse(HttpRequest request) throws IOException {
		String requestPath = request.getPath();
		for (int i = 0; i < paths.size(); i++) {
			if (requestPath.startsWith(paths.get(i))) {
				return requestHandlers.get(i).getResponse(request);
			}
		}
		throw new FileNotFoundException(request.getPath());
	}
}
