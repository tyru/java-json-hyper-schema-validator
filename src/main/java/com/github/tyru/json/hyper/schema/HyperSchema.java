package com.github.tyru.json.hyper.schema;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.json.JSONObject;

public class HyperSchema {

	public static final String DEFAULT_ENC_TYPE = "application/json";
	private static final Set<String> ACCEPTABLE_HTTP_METHODS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("POST", "PUT", "PATCH")));

	private /* final */ Map<EndPoint, Schema> routes;
	private /* final */ boolean validateMethod;
	private /* final */ boolean validateMediaType;
	private /* final */ boolean validateEntity;

	/**
	 * Endpoint can be represented by the combination of method * href *
	 * encType.
	 *
	 * 5.6.2. encType
	 * http://json-schema.org/latest/json-schema-hypermedia.html#anchor37
	 *
	 * "If no encType or method is specified, only the single URI specified by
	 * the href property is defined. If the method is POST, "application/json"
	 * is the default media type. "
	 *
	 * @author tyru
	 *
	 */
	static class EndPoint {
		private String method;
		private String href;
		private String encType;

		public String getMethod() {
			return method;
		}

		public String getHref() {
			return href;
		}

		public String getEncType() {
			return encType;
		}

		public static EndPoint of(String method, String href, String encType) {
			EndPoint obj = new EndPoint();
			obj.method = method;
			obj.href = href;
			obj.encType = encType;
			return obj;
		}
	}

	/**
	 * NOTE: This method is not intended to be used by user (You!) because
	 * internal-use only. Use {@link HyperSchemaBuilder#load(JSONObject)} to
	 * create HyperSchema object.
	 *
	 * @param routes
	 * @param doValidation
	 * @return HyperSchema
	 */
	// TODO: Create annotation to make compilation error when
	// being used by a code outside this package.
	public static HyperSchema of(Map<EndPoint, Schema> routes, boolean validateMethod, boolean validateMediaType,
			boolean validateEntity) {
		HyperSchema obj = new HyperSchema();
		obj.routes = routes;
		obj.validateMethod = validateMethod;
		obj.validateMediaType = validateMediaType;
		obj.validateEntity = validateEntity;
		return obj;
	}

	/**
	 * If a given URI path matches schema's route definitions, returns non-null
	 * Optional schema object. Otherwise, returns null Optional object.
	 *
	 * @param method
	 *            HTTP method
	 * @param href
	 *            URI path
	 * @return Optional<Schema>
	 */
	public Optional<Schema> match(String method, String href) {
		return match(method, href, DEFAULT_ENC_TYPE);
	}

	/**
	 * If a given URI path matches schema's route definitions, returns non-null
	 * Optional schema object. Otherwise, returns null Optional object.
	 *
	 * @param method
	 *            HTTP method
	 * @param href
	 *            URI path
	 * @return Optional<Schema>
	 */
	public Optional<Schema> match(String method, String href, String encType) {
		EndPoint endPoint = EndPoint.of(method, href, encType);
		return Optional.ofNullable(routes.get(endPoint));
	}

	/**
	 * @see {@link HyperSchema#validate(String, String, String, JSONObject)}
	 * @param method
	 * @param href
	 * @param jsonObject
	 */
	public void validate(String method, String href, JSONObject jsonObject) {
		validate(method, href, DEFAULT_ENC_TYPE, jsonObject);
	}

	/**
	 * Shorthand method for {@link HyperSchema#match(String, String)} and
	 * {@link Schema#validate(Object)} This method is identical to the following
	 * code:
	 * {@code match(method, href, encType).ifPresent(schema -> schema.validate(jsonObject));}
	 *
	 * @param method
	 * @param href
	 * @param jsonObject
	 */
	public void validate(String method, String href, String encType, JSONObject jsonObject) {
		match(method, href, encType).ifPresent(schema -> schema.validate(jsonObject));
	}

	/**
	 * @see {@link HyperSchema#validate(ContainerRequestContext, String)}
	 * @param context
	 * @throws IOException
	 */
	public void validate(ContainerRequestContext context) throws IOException {
		validate(context, "UTF-8");
	}

	/**
	 * JAX-RS support.
	 *
	 * @param context
	 * @throws IOException
	 */
	public void validate(ContainerRequestContext context, String charset) throws IOException {
		if (validateMethod) {
			validateHTTPMethod(context.getMethod());
		}
		if (validateMediaType) {
			validateJSONMediaType(context.getMediaType());
		}
		if (validateEntity) {
			validateEntity(context, charset);
		}
	}

	private void validateJSONMediaType(MediaType mediaType) {
		if (!"application".equals(mediaType.getType()) && "json".equals(mediaType.getSubtype())) {
			throw new IllegalArgumentException("Query media type is not 'application/json'.");
		}
	}

	private void validateHTTPMethod(String httpMethod) {
		if (!ACCEPTABLE_HTTP_METHODS.contains(httpMethod)) {
			throw new IllegalArgumentException(httpMethod + "' method does not have entity");
		}
	}

	private void validateEntity(ContainerRequestContext context, String charset) throws IOException {
		String json = getEntityWithKeepingStream(context, charset);
		if (json == null || json.isEmpty()) {
			return;
		}
		String method = context.getMethod();
		String href = context.getUriInfo().getPath();
		String encType = context.getMediaType().getType() + "/" + context.getMediaType().getSubtype();
		validate(method, href, encType, new JSONObject(json));
	}

	private String getEntityWithKeepingStream(ContainerRequestContext context, String charset) throws IOException {
		String json = IOUtils.toString(context.getEntityStream(), charset);
		// A user's controller method won't be called w/o this!
		context.setEntityStream(IOUtils.toInputStream(json));
		return json;
	}
}
