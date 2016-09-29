package me.tyru.json.hyper.schema;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.everit.json.schema.Schema;
import org.json.JSONObject;

public class HyperSchema {

	public static final String DEFAULT_ENC_TYPE = "application/json";
	private static final Set<String> ALLOW_ENTITY_METHODS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("POST", "PUT", "PATCH")));

	private final Map<EndPoint, Schema> routes;
	private final boolean validateMediaType;

	/**
	 * NOTE: This constructor is not intended to be used by user (You!) because
	 * internal-use only. Use {@link HyperSchemaBuilder#load(JSONObject)} to
	 * create HyperSchema object.
	 *
	 * @param routes
	 * @param doValidation
	 * @return HyperSchema
	 */
	// TODO: Create annotation to make compilation error when
	// being used by a code outside this package.
	HyperSchema(Map<EndPoint, Schema> routes, boolean validateMediaType) {
		Objects.requireNonNull(routes, "routes must not be null");
		Objects.requireNonNull(validateMediaType, "validateMediaType must not be null");
		this.routes = routes;
		this.validateMediaType = validateMediaType;
	}

	/**
	 * If a given URI path matches schema's route definitions, returns non-null
	 * Optional schema object. Otherwise, returns null Optional object. This is
	 * same as {@code match(method, href, DEFAULT_ENC_TYPE)}.
	 *
	 * @see {@link HyperSchema#match(String, String, String)}
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
	 * Shorthand method for {@link HyperSchema#match(String, String, String)}
	 * and {@link Schema#validate(Object)} This method is identical to the
	 * following code:
	 * {@code match(method, href, DEFAULT_ENC_TYPE).ifPresent(schema -> schema.validate(jsonObject));}
	 *
	 * @see {@link HyperSchema#validate(String, String, String, JSONObject)}
	 * @param method
	 * @param href
	 * @param jsonObject
	 */
	public void validate(String method, String href, JSONObject jsonObject) {
		validate(method, href, DEFAULT_ENC_TYPE, jsonObject);
	}

	/**
	 * Shorthand method for {@link HyperSchema#match(String, String, String)}
	 * and {@link Schema#validate(Object)} This method is identical to the
	 * following code:
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
	 * JAX-RS support. This is same as {@code validate(context, "UTF-8")}.
	 *
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
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(charset, "charset");
		Objects.requireNonNull(context.getMediaType(), "context.getMediaType()");

		if (!isJSONMediaType(context.getMediaType())) {
			// Throw or skip
			if (validateMediaType) {
				throw new IllegalArgumentException("Query media type is not 'application/json'.");
			} else {
				return;
			}
		}
		validateEntity(context, charset);
	}

	private boolean isJSONMediaType(MediaType mediaType) {
		return "application".equals(mediaType.getType()) && "json".equals(mediaType.getSubtype());
	}

	/**
	 * 5.6.3. schema
	 * http://json-schema.org/latest/json-schema-hypermedia.html#anchor38
	 *
	 * "This property contains a schema which defines the acceptable structure
	 * of the submitted request. For a GET request, this schema would define the
	 * properties for the query string and for a POST request, this would define
	 * the body."
	 *
	 * @param context
	 * @param charset
	 * @throws IOException
	 */
	private void validateEntity(ContainerRequestContext context, String charset) throws IOException {
		String method = context.getMethod();
		String href = context.getUriInfo().getPath();
		String encType = context.getMediaType().getType() + "/" + context.getMediaType().getSubtype();
		if (ALLOW_ENTITY_METHODS.contains(method)) {
			String json = getEntityWithKeepingStream(context, charset);
			if (json == null || json.isEmpty()) {
				return;
			}
			validate(method, href, encType, new JSONObject(json));
		} else {
			// This has two problems:
			// 1. Duplicate keys
			// 2. It will throw ValidatorException when the property type in
			// hyper schema is NOT "string" (because query parameters are
			// string).
			MultivaluedMap<String, String> params = context.getUriInfo().getQueryParameters();
			validate(method, href, encType, new JSONObject(params));
		}
	}

	private String getEntityWithKeepingStream(ContainerRequestContext context, String charset) throws IOException {
		String json = IOUtils.toString(context.getEntityStream(), charset);
		// A user's controller method won't be called w/o this!
		context.setEntityStream(IOUtils.toInputStream(json));
		return json;
	}
}
