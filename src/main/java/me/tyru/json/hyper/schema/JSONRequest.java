package me.tyru.json.hyper.schema;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

interface JSONRequest {

	public static JSONRequest of(HttpServletRequest request) {
		return new ServletJSONRequest(request);
	}

	public static JSONRequest of(ContainerRequestContext context) {
		return new JaxrsJSONRequest(context);
	}

	String getMethod();

	String getHref();

	String getEncType();

	String getEntityWithKeepingStream(String charset) throws IOException;

	MultivaluedMap<String, String> getQueryParameters() throws IOException;
}