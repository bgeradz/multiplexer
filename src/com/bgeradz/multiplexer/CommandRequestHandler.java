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
			public void onClose(TrackedInputStream inputStream, Throwable cause) {
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

    public static class Config implements Configurator<CommandRequestHandler> {
        private String command;
        private String paramName;

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        @Override
        public void validate() throws ConfigException {
            if (command == null) {
                throw new ConfigException("command unspecified");
            }
            if (paramName == null) {
                throw new ConfigException("paramName unspecified");
            }
        }

        @Override
        public CommandRequestHandler build() {
            return new CommandRequestHandler(command, paramName);
        }
    }
}
