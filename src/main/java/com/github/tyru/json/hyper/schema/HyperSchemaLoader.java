package com.github.tyru.json.hyper.schema;

import org.json.JSONObject;

import com.github.tyru.json.hyper.schema.HyperSchema;

public class HyperSchemaLoader {

	public static HyperSchema load(JSONObject hyperSchema) {
		return new HyperSchema(hyperSchema);
	}

}
