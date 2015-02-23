package com.bgeradz.multiplexer;

import java.io.IOException;

public class SimpleStreamRequestHandler implements HttpRequestHandler {
	private String name;
	private DataSource dataSource;
	
	public SimpleStreamRequestHandler(String name, DataSource dataSource) {
		this.name = name;
		this.dataSource = dataSource;
	}
	
	@Override
	public HttpResponse getResponse(HttpRequest request) throws IOException {
		TrackedInputStream inputStream = dataSource.open();
		HttpResponse response = new HttpResponse(request, inputStream);
		new Connection(name, inputStream, request.getOutputStream());
		return response;
	}
}
