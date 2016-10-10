package me.tyru.json.hyper.schema;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;

public class JaxrsJSONRequest implements JSONRequest {
	private ContainerRequestContext context;

	public static JaxrsJSONRequest of(ContainerRequestContext context) {
		return new JaxrsJSONRequest(context);
	}

	private JaxrsJSONRequest(ContainerRequestContext context) {
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(context.getMediaType(), "context.getMediaType()");
		Objects.requireNonNull(context.getMediaType().getType(), "context.getMediaType().getType()");
		Objects.requireNonNull(context.getMediaType().getSubtype(), "context.getMediaType().getSubtype()");
		this.context = context;
	}

	@Override
	public String getMethod() {
		return context.getMethod();
	}

	@Override
	public String getHref() {
		return context.getUriInfo().getRequestUri().getPath();
	}

	@Override
	public String getEncType() {
		return context.getMediaType().getType() + "/" + context.getMediaType().getSubtype();
	}

	/**
	 * @throws UncheckedIOException
	 */
	@Override
	public String getEntityWithKeepingStream(String charset) {
		try {
			String json = IOUtils.toString(context.getEntityStream(), charset);
			// A user's controller method won't be called w/o this!
			context.setEntityStream(IOUtils.toInputStream(json));
			return json;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		return context.getUriInfo().getQueryParameters();
	}
}