package org.org.eclipse.dws.core.internal.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class PomProfilesSet {
	private Map<String, PomProfile> pomProfiles = new HashMap<String, PomProfile>();
	
	public void addPomProfile(PomProfile pomProfile) {
		this.pomProfiles.put(pomProfile.getUID(), pomProfile);
	}

	public Map<String, PomProfile> getPomProfiles() {
		return Collections.unmodifiableMap(pomProfiles);
	}

	@Override
	public boolean equals(Object obj) {
		PomProfilesSet other = (PomProfilesSet) obj;
		return mapEquals(this.pomProfiles, other.pomProfiles);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
