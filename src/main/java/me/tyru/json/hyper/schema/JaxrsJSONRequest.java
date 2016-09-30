package me.tyru.json.hyper.schema;

import java.io.IOException;
import java.util.Objects;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;

class JaxrsJSONRequest implements JSONRequest {
	private ContainerRequestContext context;

	public JaxrsJSONRequest(ContainerRequestContext context) {
		Objects.requireNonNull(context, "context");
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
		return context.getUriInfo().getPath();
	}

	@Override
	public String getEncType() {
		return context.getMediaType().getType() + "/" + context.getMediaType().getSubtype();
	}

	@Override
	public String getEntityWithKeepingStream(String charset) throws IOException {
		String json = IOUtils.toString(context.getEntityStream(), charset);
		// A user's controller method won't be called w/o this!
		context.setEntityStream(IOUtils.toInputStream(json));
		return json;
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		return context.getUriInfo().getQueryParameters();
	}
}