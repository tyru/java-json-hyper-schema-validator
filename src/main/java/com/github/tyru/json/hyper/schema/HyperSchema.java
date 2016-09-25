package com.github.tyru.json.hyper.schema;

import java.util.Map;
import java.util.Optional;

import org.everit.json.schema.Schema;
import org.json.JSONObject;

public class HyperSchema {

	private Map<String, Schema> routes;

	public static HyperSchema of(Map<String, Schema> routes) {
		HyperSchema obj = new HyperSchema();
		obj.routes = routes;
		return obj;
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
}
