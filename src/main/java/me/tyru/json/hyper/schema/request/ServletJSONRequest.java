package me.tyru.json.hyper.schema.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;

public class ServletJSONRequest implements JSONRequest {
	private BufferedServletRequestWrapper request;
	private Supplier<MultivaluedMap<String, String>> queryParams = () -> createQueryParameters();

	public static ServletJSONRequest of(HttpServletRequest request) throws IOException {
		return new ServletJSONRequest(request);
	}

	private ServletJSONRequest(HttpServletRequest request) throws IOException {
		this.request = new BufferedServletRequestWrapper(request);
	}

	@Override
	public String getMethod() {
		return request.getMethod();
	}

	@Override
	public String getHref() {
		return request.getRequestURI();
	}

	@Override
	public String getEncType() {
		final String encType = Objects.requireNonNull(request.getContentType());
		// Get rid of string after {type}/{subtype} like "; charset=UTF-8" (XXX:
		// Is this really necessary?)
		return encType.replaceFirst(";.*", "");
	}

	/**
	 * @throws UncheckedIOException
	 */
	@Override
	public String getEntityWithKeepingStream(String charset) {
		try {
			return IOUtils.toString(request.getInputStream(), charset);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * @throws UncheckedIOException
	 */
	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		return queryParams.get();
	}

	/**
	 * This method was invoked at the first time of creation of 'queryParams'
	 * instance.
	 *
	 * @return created instance
	 */
	private synchronized MultivaluedMap<String, String> createQueryParameters() {
		if (!(queryParams instanceof CreatedSupplier)) {
			try {
				queryParams = new CreatedSupplier(request);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return queryParams.get();
	}

	private static class CreatedSupplier implements Supplier<MultivaluedMap<String, String>> {
		private final MultivaluedMap<String, String> instance;

		public CreatedSupplier(HttpServletRequest request) throws IOException {
			MultivaluedMap<String, String> tmp = new MultivaluedHashMap<>();
			BufferedServletRequestWrapper req = new BufferedServletRequestWrapper(request);
			for (String key : Collections.list(req.getParameterNames())) {
				tmp.addAll(key, req.getParameterValues(key));
			}
			instance = tmp;
		}

		/**
		 * This method was invoked at the second time or later of creation of
		 * 'queryParams' instance.
		 *
		 * @return created instance
		 */
		@Override
		public MultivaluedMap<String, String> get() {
			return instance;
		}
	}

	private static class BufferedServletInputStream extends ServletInputStream {

		private ByteArrayInputStream inputStream;

		public BufferedServletInputStream(byte[] buffer) {
			this.inputStream = new ByteArrayInputStream(buffer);
		}

		@Override
		public int available() throws IOException {
			return inputStream.available();
		}

		@Override
		public int read() throws IOException {
			return inputStream.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return inputStream.read(b, off, len);
		}

		@Override
		public boolean isFinished() {
			return inputStream.available() == 0;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener arg0) {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	private static class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

		private byte[] buffer;

		public BufferedServletRequestWrapper(HttpServletRequest request) throws IOException {
			super(request);

			InputStream is = request.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte buff[] = new byte[1024];
			int read;
			while ((read = is.read(buff)) > 0) {
				baos.write(buff, 0, read);
			}

			this.buffer = baos.toByteArray();
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return new BufferedServletInputStream(this.buffer);
		}
	}
}