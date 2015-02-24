package com.bgeradz.multiplexer;

import java.io.IOException;
import java.net.URL;

public class UrlDataSource implements DataSource {
	private URL url;
	
	public UrlDataSource(URL url) {
		this.url = url;
	}
	
	@Override
	public TrackedInputStream open() throws IOException {
		return new TrackedInputStream(url.openStream(), url.toString());
	}
}
