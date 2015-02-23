package com.bgeradz.multiplexer;

import java.io.IOException;

public interface HttpRequestHandler {
	HttpResponse getResponse(HttpRequest request) throws IOException;
}
