package one.atticus.core.resources;

import one.atticus.core.util.JsonUtil;

/**
 * A superclass for all JSON objects.
 * Overrides {@link #toString()} method in such a way that it renders an object
 * to a pretty JSON format.
 *
 * @author vgorin
 */


public abstract class AbstractJson {
	@Override
	public String toString() {
		return toPrettyJson();
	}

	public String toJson() {
		return JsonUtil.toJson(this);
	}

	public String toPrettyJson() {
		return JsonUtil.toPrettyJson(this);
	}
}
