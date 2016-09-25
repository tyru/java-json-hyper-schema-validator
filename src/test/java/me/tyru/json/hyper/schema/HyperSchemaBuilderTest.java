/**
 *
 */
package me.tyru.json.hyper.schema;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.everit.json.schema.Schema;
import org.json.JSONObject;
import org.junit.Test;

import me.tyru.json.hyper.schema.HyperSchema.EndPoint;
import mockit.Deencapsulation;

/**
 * @author tyru
 *
 */
public class HyperSchemaBuilderTest {

	/**
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#hyperSchema(org.json.JSONObject)}
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#validateMediaType(boolean)}
	 */
	@Test
	public void test_normal_hyper_schema() {
		String json = "{\"links\": [{\"rel\": \"instances\", \"method\": \"GET\", \"href\": \"/hello\", \"schema\": {}}]}";
		HyperSchema scm = HyperSchemaBuilder.hyperSchema(new JSONObject(json)).build();

		@SuppressWarnings("unchecked")
		Map<EndPoint, Schema> routes = (Map<EndPoint, Schema>)Deencapsulation.getField(scm, "routes");
		boolean validateMediaType = (boolean)Deencapsulation.getField(scm, "validateMediaType");

		assertThat(routes, is(instanceOf(Map.class)));
		assertThat(routes.size(), is(1));
		assertThat(routes, hasKey(EndPoint.of("GET", "/hello", "application/json")));
		assertThat(validateMediaType, is(false));
	}

	/**
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#hyperSchema(org.json.JSONObject)}
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#validateMediaType(boolean)}
	 */
	@Test
	public void test_missing_rel() {
		try {
			String json = "{\"links\": [{\"method\": \"GET\", \"href\": \"/hello\", \"schema\": {}}]}";
			HyperSchemaBuilder.hyperSchema(new JSONObject(json)).build();
			fail("Must throw an exception!");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#hyperSchema(org.json.JSONObject)}
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#validateMediaType(boolean)}
	 */
	@Test
	public void test_missing_href() {
		try {
			String json = "{\"links\": [{\"rel\": \"instances\", \"method\": \"GET\", \"schema\": {}}]}";
			HyperSchemaBuilder.hyperSchema(new JSONObject(json)).build();
			fail("Must throw an exception!");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#hyperSchema(org.json.JSONObject)}
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#validateMediaType(boolean)}
	 */
	@Test
	public void test_missing_method() {
		String json = "{\"links\": [{\"rel\": \"instances\", \"href\": \"/hello\", \"schema\": {}}]}";
		HyperSchema scm = HyperSchemaBuilder.hyperSchema(new JSONObject(json)).build();

		@SuppressWarnings("unchecked")
		Map<EndPoint, Schema> routes = (Map<EndPoint, Schema>)Deencapsulation.getField(scm, "routes");
		boolean validateMediaType = (boolean)Deencapsulation.getField(scm, "validateMediaType");

		assertThat(routes, is(instanceOf(Map.class)));
		assertThat(routes.size(), is(0));
		assertThat(validateMediaType, is(false));
	}

	/**
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#hyperSchema(org.json.JSONObject)}
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#validateMediaType(boolean)}
	 */
	@Test
	public void test_missing_schema() {
		String json = "{\"links\": [{\"rel\": \"instances\", \"method\": \"GET\", \"href\": \"/hello\"}]}";
		HyperSchema scm = HyperSchemaBuilder.hyperSchema(new JSONObject(json)).build();

		@SuppressWarnings("unchecked")
		Map<EndPoint, Schema> routes = (Map<EndPoint, Schema>)Deencapsulation.getField(scm, "routes");
		boolean validateMediaType = (boolean)Deencapsulation.getField(scm, "validateMediaType");

		assertThat(routes, is(instanceOf(Map.class)));
		assertThat(routes.size(), is(0));
		assertThat(validateMediaType, is(false));
	}

	/**
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#hyperSchema(org.json.JSONObject)}
	 *@see {@link me.tyru.json.hyper.schema.HyperSchemaBuilder#validateMediaType(boolean)}
	 */
	@Test
	public void test_validateMediaType() {
		String json = "{\"links\": [{\"rel\": \"instances\", \"method\": \"GET\", \"href\": \"/hello\", \"schema\": {}}]}";
		HyperSchema scm = HyperSchemaBuilder.hyperSchema(new JSONObject(json)).validateMediaType(true).build();

		@SuppressWarnings("unchecked")
		Map<EndPoint, Schema> routes = (Map<EndPoint, Schema>)Deencapsulation.getField(scm, "routes");
		boolean validateMediaType = (boolean)Deencapsulation.getField(scm, "validateMediaType");

		assertThat(routes, is(instanceOf(Map.class)));
		assertThat(routes.size(), is(1));
		assertThat(routes, hasKey(EndPoint.of("GET", "/hello", "application/json")));
		assertThat(validateMediaType, is(true));
	}
}
