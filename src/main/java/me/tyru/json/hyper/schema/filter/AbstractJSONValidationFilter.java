package me.tyru.json.hyper.schema.filter;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.json.JSONTokener;

import me.tyru.json.hyper.schema.HyperSchema;
import me.tyru.json.hyper.schema.HyperSchemaBuilder;

/**
 * JAX-RS filter of abstract class. A user can extends this class to easily
 * implement JSON validation filter.
 *
 * NOTE: A user must provide {@code @Named("JSONValidationFilter.hyperSchema")}
 * injectee to inject this class.
 *
 * @author tyru
 *
 */
@Dependent
public abstract class AbstractJSONValidationFilter implements ContainerRequestFilter {

	/**
	 * This bean will be injected ordinarily by derived class on another
	 * project.
	 */
	@SuppressWarnings("cdi-ambiguous-dependency")
	@Inject
	@Named("JSONValidationFilter.hyperSchema")
	private HyperSchema hyperSchema;

	static public HyperSchema createHyperSchema(String filename) throws IOException {
		try (InputStream inputStream = AbstractJSONValidationFilter.class.getResourceAsStream(filename)) {
			JSONObject json = new JSONObject(new JSONTokener(inputStream));
			return HyperSchemaBuilder.hyperSchema(json).build();
		}
	}

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		try {
			// Throws a ValidationException if accepted json is invalid
			hyperSchema.validate(context);
		} catch (ValidationException e) {
			context.abortWith(Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}
	}
}