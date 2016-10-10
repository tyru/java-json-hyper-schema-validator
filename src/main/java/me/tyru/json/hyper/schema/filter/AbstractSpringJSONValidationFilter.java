package me.tyru.json.hyper.schema.filter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import me.tyru.json.hyper.schema.HyperSchema;
import me.tyru.json.hyper.schema.JSONRequest;
import me.tyru.json.hyper.schema.SpringJSONRequest;

public abstract class AbstractSpringJSONValidationFilter extends RequestBodyAdviceAdapter {

	@Autowired
	@Qualifier("AbstractSpringJSONValidationFilter.hyperSchema")
	private HyperSchema hyperSchema;

	private String json;

	@Override
	public boolean supports(MethodParameter paramMethodParameter, Type paramType,
			Class<? extends HttpMessageConverter<?>> paramClass) {
		return true;
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		this.json = IOUtils.toString(inputMessage.getBody());
		final InputStream returnInputStream = IOUtils.toInputStream(json);
		final HttpHeaders returnHeaders = inputMessage.getHeaders();

		return new HttpInputMessage() {
			@Override
			public InputStream getBody() throws IOException {
				return returnInputStream;
			}
			@Override
			public HttpHeaders getHeaders() {
				return returnHeaders;
			}
		};
	}

	@InitBinder
	private void initBinder(
			WebDataBinder binder,
			ServletWebRequest request) throws IOException
	{
		try {
			// Throws a ValidationException if accepted json is invalid
			HttpHeaders headers = new HttpHeaders();
			request.getHeaderNames().forEachRemaining(name -> {
				headers.add(name, request.getHeader(name));
			});
			final JSONRequest req = SpringJSONRequest.of(
					request.getHttpMethod().toString(), URI.create(request.getRequest().getRequestURI()),
					headers , this.json);
			hyperSchema.validate(req);
		} finally {
			this.json = null;
		}
	}
}
