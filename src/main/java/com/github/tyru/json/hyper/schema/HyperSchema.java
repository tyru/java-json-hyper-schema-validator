package com.github.tyru.json.hyper.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

public class HyperSchema {

	private Map<String, Schema> routes;

	public HyperSchema(JSONObject hyperSchema) {
		Objects.requireNonNull(hyperSchema, "hyperSchema must not be null");
		requireKey(hyperSchema, "links", JSONArray.class, "/links");
		Map<String, Schema> routes = new HashMap<>();
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
				routes.put(linkDef.get("method") + " " + href, SchemaLoader.load(linkDef.getJSONObject("schema")));
			}
		}
		this.routes = routes;
	}

	public Optional<Schema> match(String method, String path) {
		return Optional.ofNullable(routes.get(method + " " + path));
	}

	/**
	 * Shorthand method for {@link HyperSchema#match(String, String)} and {@link Schema#validate(Object)}
	 * @param method
	 * @param path
	 * @param jsonObject
	 */
	public void validate(String method, String path, JSONObject jsonObject) {
		match(method, path).ifPresent(schema -> schema.validate(jsonObject));
	}

	private void requireKey(JSONObject hyperSchema, String key, Class<?> cls, String pointer) {
		if (!hyperSchema.has(key)) {
			throw new IllegalArgumentException("'" + pointer + " must exist in JSON");
		}
		if (!cls.isInstance(hyperSchema.get(key))) {
			throw new IllegalArgumentException("'" + pointer + "' must be the instance of " + cls.getName());
		}
	}

	private void requireClassIs(Object obj, Class<?> cls, String msg) {
		if (!cls.isInstance(obj)) {
			throw new IllegalArgumentException(msg + " is not " + cls.getName());
		}
	}
}
