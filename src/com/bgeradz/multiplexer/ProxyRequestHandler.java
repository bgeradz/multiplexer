package com.bgeradz.multiplexer;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class ProxyRequestHandler implements HttpRequestHandler {
	private static final Logger L = App.createLogger(ProxyRequestHandler.class.getSimpleName());
	private String urlBase;

	public ProxyRequestHandler(String urlBase) {
		this.urlBase = urlBase;
	}

	@Override
	public HttpResponse getResponse(HttpRequest request) throws IOException {
		StringBuilder buf = new StringBuilder(urlBase);
		Map<String, String> params = request.getParams();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			buf.append(first ? '?' : '&');
			first = false;
			buf.append(URLEncoder.encode(entry.getKey(), "utf-8"));
			buf.append('=');
			buf.append(URLEncoder.encode(entry.getValue(), "utf-8"));
		}

		String surl = buf.toString();
		L.info("Relaying to "+ surl);
		URL url = new URL(surl);
		TrackedInputStream input = new TrackedInputStream(url.openStream());
		new Connection(urlBase, input, request.getOutputStream());
		HttpResponse response = new HttpResponse(request, input);
		return response;
	}
}
