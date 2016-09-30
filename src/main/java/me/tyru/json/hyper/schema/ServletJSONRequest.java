package me.tyru.json.hyper.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;

class ServletJSONRequest implements JSONRequest {
	private HttpServletRequest request;

	public ServletJSONRequest(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getMethod() {
		// XXX
		return request.getMethod();
	}

	@Override
	public String getHref() {
		// XXX
		return request.getRequestURI();
	}

	@Override
	public String getEncType() {
		// XXX
		return request.getContentType();
	}

	@Override
	public String getEntityWithKeepingStream(String charset) throws IOException {
		return IOUtils.toString(new BufferedServletRequestWrapper(request).getInputStream(), charset);
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}

class BufferedServletInputStream extends ServletInputStream {

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

class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

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
