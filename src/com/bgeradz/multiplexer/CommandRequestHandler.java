package com.bgeradz.multiplexer;

import java.io.IOException;

public class CommandRequestHandler implements HttpRequestHandler {
	String command;
	String paramName;
	
	public CommandRequestHandler(String command, String paramName) {
		this.command = command;
		this.paramName = paramName;
	}

	@Override
	public HttpResponse getResponse(HttpRequest request) throws IOException {
		String param = request.getParam(paramName);
		final Process process = Runtime.getRuntime().exec(new String[]{command, param});
		TrackedInputStream input = new TrackedInputStream(process.getInputStream(), command);
		input.addTracker(new IOTrackerAdapter() {
			@Override
			public void onClose(TrackedInputStream inputStream, IOException cause) {
				try {
					process.waitFor();
					process.destroy();
				} catch (InterruptedException e) {}
			}			
		});
		new Connection(input, request.getOutputStream());
		HttpResponse response = new HttpResponse(request, input);	
		return response;
	}
}
