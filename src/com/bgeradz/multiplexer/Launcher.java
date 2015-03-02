package com.bgeradz.multiplexer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Launcher {
	private static final Logger L = App.createLogger(Launcher.class.getSimpleName());

	private Coordinator coordinator = new Coordinator();
	
	private Launcher(Config config) {
        this.coordinator = config.coordinator;
        App.setSpeedMeasurerCapacity(config.getSpeedMeasurerCapacity());

		HttpServer httpServer = new HttpServer(config.address, config.port, coordinator);
		try {
			httpServer.start();
		} catch (IOException e) {
			L.error("Cannot start HTTP server", e);
		}
	}
	
    public static class Config implements Configurator<Launcher> {
        private Coordinator coordinator = new Coordinator();
        private String address = "0.0.0.0";
        private int port;
        private int speedMeasurerCapacity = App.DEFAULT_SPEED_MEASURER_CAPACITY;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void addHandler(String path, Configurator<HttpRequestHandler> handler) {
            coordinator.addRequestHandler(path, handler.build());
        }

        public int getSpeedMeasurerCapacity() {
            return speedMeasurerCapacity;
        }

        public void setSpeedMeasurerCapacity(int speedMeasurerCapacity) {
            this.speedMeasurerCapacity = speedMeasurerCapacity;
        }

        @Override
        public void validate() throws ConfigException {
            if (port == 0) {
                throw new ConfigException("port unspecified");
            }
        }

        @Override
        public Launcher build() {
            return new Launcher(this);
        }
    }

}
