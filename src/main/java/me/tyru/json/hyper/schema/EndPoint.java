package me.tyru.json.hyper.schema;

import java.util.Objects;

/**
 * Endpoint can be represented by the combination of method, href,
 * encType.
 *
 * 5.6.2. encType
 * http://json-schema.org/latest/json-schema-hypermedia.html#anchor37
 *
 * "If no encType or method is specified, only the single URI specified by
 * the href property is defined. If the method is POST, "application/json"
 * is the default media type. "
 *
 * @author tyru
 *
 */
public class EndPoint {
	private String method;
	private String href;
	private String encType;

	private EndPoint() {
	}

	public static EndPoint of(String method, String href, String encType) {
		Objects.requireNonNull(method, "method must not be null");
		Objects.requireNonNull(href, "href must not be null");
		Objects.requireNonNull(encType, "encType must not be null");
		EndPoint obj = new EndPoint();
		obj.method = method;
		obj.href = href;
		obj.encType = encType;
		return obj;
	}

	public String getMethod() {
		return method;
	}

	public String getHref() {
		return href;
	}

	public String getEncType() {
		return encType;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((encType == null) ? 0 : encType.hashCode());
		result = prime * result + ((href == null) ? 0 : href.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EndPoint other = (EndPoint) obj;
		if (encType == null) {
			if (other.encType != null)
				return false;
		} else if (!encType.equals(other.encType))
			return false;
		if (href == null) {
			if (other.href != null)
				return false;
		} else if (!href.equals(other.href))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}
}