package local.radioschedulers.run.http;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.servlet.MultiPartFilter;

public class RunHTTPServer {

	public static void startServer(Handler h) throws Exception {
		Handler handler = getHandler();
		Server server = new Server(8080);
		server.setHandler(handler);
		server.start();
	}

	private static Handler getHandler() {
		Handler handler = new AbstractHandler() {
			public void handle(String target, HttpServletRequest request,
					HttpServletResponse response, int dispatch)
					throws IOException, ServletException {
				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_OK);
				PrintWriter w = response.getWriter();
				w.println("<h1>Hello</h1>");
				w.println("<pre>");
				w.println("target = " + target);
				w.println("request.method = " + request.getMethod());
				w.println("request.uri = " + request.getRequestURI());
				MultiPartFilter mpf = new MultiPartFilter();
				mpf.doFilter(request, response, getFileHandleFilter());
				w.println("</pre>");
				((Request) request).setHandled(true);
			}
		};
		return handler;
	}

	protected static FilterChain getFileHandleFilter() {
		return new FilterChain() {

			@Override
			public void doFilter(ServletRequest request,
					ServletResponse response) throws IOException,
					ServletException {
				File f = (File) request.getAttribute("filename1");
				response.getWriter().println("file exists: " + f.exists());
				
			}

		};
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
