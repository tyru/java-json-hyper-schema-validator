package me.tyru.json.hyper.schema;

import java.net.URI;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class SpringJSONRequest implements JSONRequest {
		private final String method;
		private final URI requestUri;
		private final HttpHeaders headers;
		private final String body;

		public static SpringJSONRequest of(
				final String method, final URI requestUri,
				final HttpHeaders headers, final String body)
		{
			return new SpringJSONRequest(method, requestUri, headers, body);
		}
		private SpringJSONRequest(
				final String method, final URI requestUri,
				final HttpHeaders headers, final String body)
		{
			this.method = method;
			this.requestUri = requestUri;
			this.headers = headers;
			this.body = body;
		}

		@Override
		public String getMethod() {
			return method;
		}

		@Override
		public String getHref() {
			return requestUri.getPath();
		}

		@Override
		public String getEncType() {
			final MediaType contentType = headers.getContentType();
			return contentType.getType() + "/" + contentType.getSubtype();
		}

		// TODO: Respect 'charset'.
		@Override
		public String getEntityWithKeepingStream(String charset) {
			return body;
		}

		@Override
		public MultivaluedMap<String, String> getQueryParameters() {
		    return Arrays.stream(requestUri.getQuery().split("&"))
		            .map(this::splitQueryParameter)
		            .collect(Collectors.groupingBy(
		            		SimpleImmutableEntry::getKey,
		            		MultivaluedHashMap::new,
		            		Collectors.mapping(MultivaluedHashMap.Entry::getValue, Collectors.toList())));
		}

		private SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
		    final int idx = it.indexOf("=");
		    final String key = idx > 0 ? it.substring(0, idx) : it;
		    final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
		    return new SimpleImmutableEntry<>(key, value);
		}
	}