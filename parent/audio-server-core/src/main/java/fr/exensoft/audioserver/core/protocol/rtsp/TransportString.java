package fr.exensoft.audioserver.core.protocol.rtsp;

import java.util.LinkedHashMap;
import java.util.Map;

public class TransportString {
	private Map<String, Object> mValues;

	public TransportString(String data) {
		mValues = new LinkedHashMap<String, Object>();
		parse(data);
	}

	public boolean has(String key) {
		return mValues.containsKey(key);
	}

	public void parse(String data) {
		mValues.clear();
		String[] values = data.split(";");
		for (String value : values) {
			if (value.trim().isEmpty()) continue;
			if (value.contains("=")) {
				String[] pair = value.split("=", 2);
				mValues.put(pair[0].trim(), pair[1].trim());
			}
			else {
				mValues.put(value, true);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, Object> pair : mValues.entrySet()) {
			if (first) first = false;
			else sb.append(";");
			sb.append(pair.getKey());
			if (pair.getValue() instanceof String) sb.append("=").append(pair.getValue());
		}
		return sb.toString();
	}

	public void set(String key, String value) {
		mValues.put(key, value);
	}

	public void setTag(String key) {
		mValues.put(key, true);
	}

	public String getString(String key) {
		Object value = mValues.get(key);
		if (value == null) return null;
		return value.toString();
	}

}
