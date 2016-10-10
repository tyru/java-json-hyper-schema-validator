package me.tyru.json.hyper.schema;

import java.io.UncheckedIOException;

import javax.ws.rs.core.MultivaluedMap;

public interface JSONRequest {
	String getMethod();
	String getHref();
	String getEncType();
	/**
	 * @param charset
	 * @return
	 * @throws UncheckedIOException
	 */
	String getEntityWithKeepingStream(String charset);
	/**
	 * @return
	 * @throws UncheckedIOException
	 */
	MultivaluedMap<String, String> getQueryParameters();
}