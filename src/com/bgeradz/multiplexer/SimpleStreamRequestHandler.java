package com.bgeradz.multiplexer;

import java.io.IOException;

public class SimpleStreamRequestHandler implements HttpRequestHandler {
	private DataSource dataSource;
    private String mimeType;

	public SimpleStreamRequestHandler(Config config) {
		dataSource = config.getDataSorce().build();
        mimeType = config.getMimeType();
	}

	@Override
	public HttpResponse getResponse(HttpRequest request) throws IOException {
		TrackedInputStream inputStream = dataSource.open();
		HttpResponse response = new HttpResponse(request, inputStream);
        if (mimeType != null) {
            response.setMimeType(mimeType);
        }
		new Connection(inputStream, request.getOutputStream());
		return response;
	}

    public static class Config implements Configurator<SimpleStreamRequestHandler> {
        private Configurator<DataSource> dataSource;
        private String mimeType;

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public void setDataSource(Configurator<DataSource> dataSource) {
            this.dataSource = dataSource;
        }

        public Configurator<DataSource> getDataSorce() {
            return dataSource;
        }

        @Override
        public void validate() throws ConfigException {
            if (dataSource == null) {
                throw new ConfigException("dataSource unspecified");
            }
        }

        @Override
        public SimpleStreamRequestHandler build() {
            return new SimpleStreamRequestHandler(this);
        }
    }
}
