package com.github.tyru.json.hyper.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

public class HyperSchemaLoader {

	/**
	 * Construct {@link HyperSchema} object from a JSONObject of JSON Hyper
	 * Schema.
	 *
	 * @param hyperSchema
	 *            JSONObject of JSON Hyper Schema
	 * @return HyperSchema
	 */
	public static HyperSchema load(JSONObject hyperSchema) {
		Objects.requireNonNull(hyperSchema, "hyperSchema must not be null");
		requireKey(hyperSchema, "links", JSONArray.class, "/links");
		Map<HyperSchema.EndPoint, Schema> routes = new HashMap<>();
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
				String encType = linkDef.has("encType") && linkDef.get("encType") instanceof String ?
						linkDef.getString("encType") : HyperSchema.DEFAULT_ENC_TYPE;
				HyperSchema.EndPoint endPoint = HyperSchema.EndPoint.of(linkDef.getString("method"), href, encType);
				routes.put(endPoint, SchemaLoader.load(linkDef.getJSONObject("schema")));
			}
		}
		return HyperSchema.of(routes);
	}

	private static void requireKey(JSONObject hyperSchema, String key, Class<?> cls, String pointer) {
		if (!hyperSchema.has(key)) {
			throw new IllegalArgumentException("'" + pointer + " must exist in JSON");
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
