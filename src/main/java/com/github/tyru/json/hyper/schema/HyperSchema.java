package com.github.tyru.json.hyper.schema;

import java.util.Map;
import java.util.Optional;

import org.everit.json.schema.Schema;
import org.json.JSONObject;

public class HyperSchema {

	public static final String DEFAULT_ENC_TYPE = "application/json";
	private Map<EndPoint, Schema> routes;

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
	 * internal-use only. Use {@link HyperSchemaLoader#load(JSONObject)} to
	 * create HyperSchema object.
	 *
	 * @param routes
	 * @return HyperSchema
	 */
	// TODO: Create annotation to make compilation error when
	// being used by a code outside this package.
	public static HyperSchema of(Map<EndPoint, Schema> routes) {
		HyperSchema obj = new HyperSchema();
		obj.routes = routes;
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
}
