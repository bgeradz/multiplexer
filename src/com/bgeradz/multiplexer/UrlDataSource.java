package com.bgeradz.multiplexer;

import java.io.IOException;
import java.net.MalformedURLException;
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

    public static class Config implements Configurator<UrlDataSource> {
        private URL url;

        public void setUrl(String url) throws ConfigException {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException e) {
                throw new ConfigException(e);
            }
        }
        public String getUrl() {
            return url == null ? null : url.toString();
        }

        @Override
        public void validate() throws ConfigException {
            if (url == null) {
                throw new ConfigException("url unspecified");
            }
        }

        @Override
        public UrlDataSource build() {
            return new UrlDataSource(url);
        }
    }

}
