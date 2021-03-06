package com.bgeradz.multiplexer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
        private NumberFormat speedFormatter = new DecimalFormat("#0.00 kB/s");
        private NumberFormat transferFormatterB = new DecimalFormat("#0 B");
        private NumberFormat transferFormatterKb = new DecimalFormat("#0.000 kB");
        private NumberFormat transferFormatterMb = new DecimalFormat("#0.000 MB");
        private NumberFormat transferFormatterGb = new DecimalFormat("#0.000 GB");

		public byte[] create() throws IOException {			
			out.println("<html>");
			out.println("<head>");
			out.println("  <title>Status</title>");
			out.println("  <style>");
			out.println("    td {");
			out.println("      padding-left:5px;");
			out.println("      padding-right:5px;");
			out.println("    }");
            out.println("    .transferred {");
            out.println("      text-align: right;");
            out.println("    }");
            out.println("    .speed {");
            out.println("      text-align: right;");
            out.println("    }");

			out.println("  </style>");
			out.println("</head>");
			out.println("<body>");
			out.println("<table>");
			
			out.println("  <tr>");
			out.println("    <th>#</th>");
			out.println("    <th>Input</th>");
			out.println("    <th>Output</th>");
			out.println("    <th>State</th>");
			out.println("    <th class=\"transferred\">Transferred</th>");
            out.println("    <th class=\"speed\">Rate</th>");
			out.println("  </tr>");
			
			long now = System.currentTimeMillis();
			
			List<Connection> connections = App.getConnections();
			for (int i = 0; i < connections.size(); i++) {
				Connection connection = connections.get(i);
				
				String stateString = connection.getState().toString();
				stateString += " ("+ formatTimeAgo(now, connection.getLastStateChangeTime(), "since ", "") + ")";
				
				TrackedInputStream input = connection.getInput();
				TrackedOutputStream output = connection.getOutput();
				
				out.println("  <tr>");
				out.println("    <td>"+ i +"</td>");
				out.println("    <td>"+ input.getName() +"</td>");
				out.println("    <td>"+ output.getName() +"</td>");
				out.println("    <td>"+ stateString +"</td>");
				out.println("    <td class=\"transferred\">"+ formatTransferred(connection.getBytesTransferred()) +"</td>");
                out.println("    <td class=\"speed\">"+ formatSpeed(connection.getAverageSpeed()) +"</td>");
				out.println("  </tr>");
			}
			
			out.println("</table>");
			
			out.println("<br />");

			int pos;
			
			out.println("<br />");
			out.println("Open input streams: <br />");
			out.println("<table>");
			out.println("  <tr>");
			out.println("    <th>#</th>");
			out.println("    <th>Stream</th>");
			out.println("    <th>Status</th>");
			out.println("  </tr>");

			pos = 1;
			for (TrackedInputStream input : App.getTrackedInputStreams()) {
				String lastRead = formatTimeAgo(now, input.getLastReadTime(), "", " ago");
				out.println("  <tr>");
				out.println("    <td>" + (pos++) +"</td>");
				out.println("    <td>" + input.getName() +"</td>");
				out.println("    <td>(last read: "+lastRead+ ")</td>");
				out.println("  </tr>");
			}
			out.println("</table>");
			

			out.println("<br />");
			out.println("Open output streams: <br>");
			out.println("<table>");
			out.println("  <tr>");
			out.println("    <th>#</th>");
			out.println("    <th>Stream</th>");
			out.println("    <th>Status</th>");
			out.println("  </tr>");
			pos = 1;
			for (TrackedOutputStream output : App.getTrackedOutputStreams()) {
				String lastWrite = formatTimeAgo(now, output.getLastWriteTime(), "", " ago");
				out.println("  <tr>");
				out.println("    <td>" + (pos++) +"</td>");
				out.println("    <td>" + output.getName() +"</td>");
				out.println("    <td>(last write: "+ lastWrite +")</td>");
				out.println("  </tr>");
			}
			out.println("</table>");

			out.println("</body></html>");
			
			out.flush();
			return outStream.toByteArray();
		}
		
		private String formatTimeAgo(long now, long event, String prefix, String suffix) {
			if (event == 0L) {
				return "never";
			} else {
				long elapsed = now - event;
				if (elapsed < 1000) {
					return prefix + elapsed + " ms" + suffix;
				} else {
					return prefix + (elapsed / 1000) + " sec" + suffix;
				}
			}
		}

        private String formatSpeed(double speed) {
            return speedFormatter.format(speed / 1024.0);
        }

        private String formatTransferred(long trans) {
            double transferred = (double) trans;
            if (trans < 1024) {
                return transferFormatterB.format(transferred);
            } else if (trans < 1024 * 1024) {
                return transferFormatterKb.format(transferred / 1024.0);
            } else if (trans < 1024 * 1024 * 1024) {
                return transferFormatterMb.format(transferred / 1024.0 / 1024.0);
            } else {
                return transferFormatterGb.format(transferred / 1024.0 / 1024.0 / 1024.0);
            }
        }
	}

    public static class Config implements Configurator<StatusRequestHandler> {
        @Override
        public void validate() throws ConfigException {
        }

        @Override
        public StatusRequestHandler build() {
            return new StatusRequestHandler();
        }
    }
}
