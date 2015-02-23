package com.bgeradz.multiplexer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Runnable {
	private static final Logger L = App.createLogger(HttpServer.class.getSimpleName());
	
	private String addr;
	private int port;
	private ServerSocket serverSocket;
	private Thread listenThread;
	private Coordinator coordinator;
	
	public HttpServer(String addr, int port, Coordinator coordinator) {
		this.addr = addr;
		this.port = port;
		this.coordinator = coordinator;
	}
	
	public void start() throws IOException {
		InetAddress inetAddress = InetAddress.getByName(addr);
		serverSocket = new ServerSocket(port, 50, inetAddress);
		listenThread = new Thread(this);
		listenThread.start();
		L.info("HttpServer listening on "+ addr +":"+ port);
	}
	
	public void run() {
		try {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				HttpRequest request = new HttpRequest(clientSocket);
				Worker worker = new Worker(request);
				worker.start();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private class Worker extends Thread {
		private HttpRequest request;
		
		public Worker(HttpRequest request) {
			this.request = request;
		}
		
		@Override
		public void run() {
			try {
				request.readHeaders();
				HttpResponse response = coordinator.getResponse(request);
				response.transfer();
			} catch (FileNotFoundException e) {
				L.warn("Not found "+ e.getMessage());
				errorResponse(404, "Not found");
			} catch (IOException e) {
			} finally {
				Util.close(request);
			}
		}
		
		private void errorResponse(int status, String message) {
			HttpResponse response = new HttpResponse(request, status, message);
			try {
				response.transfer();
			} catch (IOException e) {
			} finally {
				request.close();
				response.close();
			}
		}
	}

}
