package org.org.eclipse.dws.core.internal.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@SuppressWarnings("unchecked")
public class PomPropertiesSet {
	private Map<String, PomProperty> properties = new HashMap<String, PomProperty>();

	public void addProperties(Map<String, String> properties) {
		for (String key : properties.keySet()) {
			if (!this.properties.containsKey(key)) {
				this.properties.put(key, new PomProperty(key));
			}
			this.properties.get(key).addPossibleValue(properties.get(key));
		}
	}

	public void addProperty(PomProperty property) {
		if (!this.properties.containsKey(property.getKey())) {
			this.properties.put(property.getKey(), property);
		}
		this.properties.get(property.getKey()).addPossibleValues(property.getPossibleValues().<String> toArray(new String[0]));
	}

	public Map<String, PomProperty> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	public PomProperty getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public boolean equals(Object obj) {
		PomPropertiesSet other = (PomPropertiesSet) obj;
		return mapEquals(this.properties, other.properties);
	}

	@SuppressWarnings("rawtypes")
	private boolean mapEquals(Map<?, ?> expected, Map<?, ?> actual) {
		boolean result = true;
		Object[] expectedSorted = new TreeSet(expected.keySet()).toArray();
		Object[] actualSorted = new TreeSet(actual.keySet()).toArray();
		result = result && Arrays.deepEquals(expectedSorted, actualSorted);
		if (result) {
			for (Object key : expectedSorted) {
				result = result && expected.get(key).equals(actual.get(key));
			}
		}
		return result;
	}
}
