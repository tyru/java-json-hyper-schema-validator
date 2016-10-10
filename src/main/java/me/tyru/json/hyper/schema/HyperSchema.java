package me.tyru.json.hyper.schema;

import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

import org.everit.json.schema.Schema;
import org.json.JSONObject;

import me.tyru.json.hyper.schema.request.JSONRequest;
import me.tyru.json.hyper.schema.request.JaxrsJSONRequest;
import me.tyru.json.hyper.schema.request.ServletJSONRequest;

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
		this.routes = Objects.requireNonNull(routes, "routes must not be null");
		this.validateMediaType = Objects.requireNonNull(validateMediaType, "validateMediaType must not be null");
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
	 * @throws UncheckedIOException
	 */
	public void validate(ContainerRequestContext context) {
		validate(JaxrsJSONRequest.of(context), "UTF-8");
	}

	/**
	 * JAX-RS support. This is same as {@code validate(context, "UTF-8")}.
	 *
	 * @param context
	 * @throws UncheckedIOException
	 */
	public void validate(ContainerRequestContext context, String charset) {
		validate(JaxrsJSONRequest.of(context), charset);
	}

	/**
	 * HttpServletRequest support. This is same as
	 * {@code validate(request, "UTF-8")}.
	 *
	 * @see {@link HyperSchema#validate(HttpServletRequest, String)}
	 * @param request
	 * @throws UncheckedIOException
	 */
	public void validate(HttpServletRequest request) {
		validate(ServletJSONRequest.of(request), "UTF-8");
	}

	/**
	 * HttpServletRequest support.
	 *
	 * @param request
	 * @throws UncheckedIOException
	 */
	public void validate(HttpServletRequest request, String charset) {
		validate(ServletJSONRequest.of(request), charset);
	}

	/**
	 * Validate JSONRequest type's value which may be defined by user by
	 * implementing JSONRequest interface.
	 *
	 * @param request
	 * @throws UncheckedIOException
	 */
	public void validate(JSONRequest req) {
		validate(req, "UTF-8");
	}

	/**
	 * Validate JSONRequest type's value which may be defined by user by
	 * implementing JSONRequest interface.
	 *
	 * @param request
	 * @param charset
	 * @throws UncheckedIOException
	 */
	public void validate(JSONRequest req, String charset) {
		Objects.requireNonNull(req, "request");
		Objects.requireNonNull(charset, "charset");
		Objects.requireNonNull(req.getEncType(), "req.getEncType()");

		if (!"application/json".equals(req.getEncType())) {
			// Throw or skip
			if (validateMediaType) {
				throw new IllegalArgumentException("Query media type is not 'application/json'.");
			} else {
				return;
			}
		}
		validateEntity(req, charset);
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
	 * @throws UncheckedIOException
	 */
	private void validateEntity(JSONRequest req, String charset) {
		String method = req.getMethod();
		String href = req.getHref();
		String encType = req.getEncType();
		if (ALLOW_ENTITY_METHODS.contains(method)) {
			String json = req.getEntityWithKeepingStream(charset);
			if (json == null || json.isEmpty()) {
				// TODO: Must above methods contain entity?
				return;
			}
			validate(method, href, encType, new JSONObject(json));
		} else {
			// TODO
			// This has two problems:
			// 1. Duplicate keys
			// 2. It will throw ValidatorException when the property type in
			// hyper schema is NOT "string" (because query parameters are
			// string).
			validate(method, href, encType, new JSONObject(req.getQueryParameters()));
		}
	}
}
