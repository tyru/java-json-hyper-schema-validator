package me.tyru.json.hyper.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import me.tyru.json.hyper.schema.exception.DuplicateLinkDefinitionException;

public class HyperSchemaBuilder {

	private JSONObject hyperSchema;
	private boolean validateMediaType = false;

	private HyperSchemaBuilder() {
	}

	/**
	 * A start point of builder chains. Sets a hyper schema object which
	 * represents JSON hyper schema. NOTE: This method is static method.
	 *
	 * @param hyperSchema
	 * @return HyperSchemaBuilder
	 */
	public static HyperSchemaBuilder hyperSchema(JSONObject hyperSchema) {
		Objects.requireNonNull(hyperSchema, "hyperSchema must not be null");
		HyperSchemaBuilder builder = new HyperSchemaBuilder();
		builder.hyperSchema = hyperSchema;
		return builder;
	}

	/**
	 * If this flag is set to false, media type is not validated. Default is
	 * false.
	 *
	 * @param validateMediaType
	 * @return HyperSchemaBuilder
	 */
	public HyperSchemaBuilder validateMediaType(boolean validateMediaType) {
		this.validateMediaType = validateMediaType;
		return this;
	}

	/**
	 * Construct {@link HyperSchema} object from a JSONObject of JSON Hyper
	 * Schema.
	 *
	 * @param hyperSchema
	 *            JSONObject of JSON Hyper Schema
	 * @return HyperSchema
	 */
	public HyperSchema build() {
		Objects.requireNonNull(hyperSchema, "hyperSchema must not be null");
		requireKey(hyperSchema, "links", JSONArray.class, "/links");
		Map<EndPoint, Schema> routes = new HashMap<>();
		JSONArray links = hyperSchema.getJSONArray("links");
		for (int i = 0; i < links.length(); i++) {
			Object obj = links.get(i);
			requireClassIs(obj, JSONObject.class, "/links/" + i);
			JSONObject linkDef = (JSONObject) obj;
			// Required by JSON Hyper-Schema spec
			requireKey(linkDef, "href", String.class, "/links/" + i + "/href");
			requireKey(linkDef, "rel", String.class, "/links/" + i + "/rel");
			String href = linkDef.getString("href");
			if (linkDef.has("method") && linkDef.has("schema")) {
				requireClassIs(linkDef.get("method"), String.class, "/links/" + i + "/method");
				requireClassIs(linkDef.get("schema"), JSONObject.class, "/links/" + i + "/schema");
				String encType = linkDef.has("encType") && linkDef.get("encType") instanceof String
						? linkDef.getString("encType") : HyperSchema.DEFAULT_ENC_TYPE;
				EndPoint endPoint = EndPoint.of(linkDef.getString("method"), href, encType);
				if (!routes.containsKey(endPoint)) {
					routes.put(endPoint, SchemaLoader.load(linkDef.getJSONObject("schema")));
				} else {
					throw new DuplicateLinkDefinitionException("Duplicate key: " + endPoint.toString());
				}
			}
		}
		return new HyperSchema(routes, validateMediaType);
	}

	private static void requireKey(JSONObject hyperSchema, String key, Class<?> cls, String pointer) {
		if (!hyperSchema.has(key)) {
			throw new IllegalArgumentException("'" + pointer + "' must exist in JSON");
		}
		if (!cls.isInstance(hyperSchema.get(key))) {
			throw new IllegalArgumentException("'" + pointer + "' must be the instance of " + cls.getName());
		}
	}

	private static void requireClassIs(Object obj, Class<?> cls, String msg) {
		if (!cls.isInstance(obj)) {
			throw new IllegalArgumentException(msg + " is not " + cls.getName());
		}
	}
}
