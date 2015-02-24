package com.bgeradz.multiplexer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class StatusRequestHandler implements HttpRequestHandler {

	@Override
	public HttpResponse getResponse(HttpRequest request) throws IOException {
		byte[] answer = new Answer().create();
		TrackedInputStream input = new TrackedInputStream(new ByteArrayInputStream(answer), "status");
		new Connection(input, request.getOutputStream());
		return new HttpResponse(request, input);
	}
	
	private class Answer {
		private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		private PrintWriter out = new PrintWriter(outStream);

		public byte[] create() throws IOException {			
			out.println("<html><body>");
			out.println("<table>");
			
			out.println("  <tr>");
			out.println("    <th>#</th>");
			out.println("    <th>Name</th>");
			out.println("    <th>State</th>");
			out.println("    <th>Transferred</th>");
			out.println("  </tr>");
			
			List<Connection> connections = App.getConnections();
			for (int i = 0; i < connections.size(); i++) {
				Connection connection = connections.get(i);
				
				String stateString = connection.getState().toString();
				long now = System.currentTimeMillis();
				long stateDuration = now - connection.getLastStateChangeTime();
				if (now > 1000) {
					stateString += " ("+ stateDuration +"ms)";
				}
				
				TrackedInputStream input = connection.getInput();
				TrackedOutputStream output = connection.getOutput();
				String desc = input.getName() + " => " + output.getName();
				
				out.println("  <tr>");
				out.println("    <td>"+ i +"</td>");
				out.println("    <td>"+ desc +"</td>");
				out.println("    <td>"+ stateString +"</td>");
				out.println("    <td>"+ connection.getBytesTransferred() +"</td>");
				out.println("  </tr>");
			}
			
			out.println("</table>");
			
			out.println("Open input streams: <br />");
			for (TrackedInputStream input : App.getTrackedInputStreams()) {
				out.println("&nbsp;&nbsp;&nbsp" + input.getName() + "<br />");
			}
			out.println("<br />");
			out.println("Open output streams: <br>");
			for (TrackedOutputStream output : App.getTrackedOutputStreams()) {
				out.println("&nbsp;&nbsp;&nbsp" + output.getName() + "<br />");
			}
			
			
			out.println("</body></html>");
			
			out.flush();
			return outStream.toByteArray();
		}
	}
}
