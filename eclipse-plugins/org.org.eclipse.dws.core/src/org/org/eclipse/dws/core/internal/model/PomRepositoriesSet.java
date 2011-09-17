package org.org.eclipse.dws.core.internal.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PomRepositoriesSet {
	private Map<String, PomRepository> pomRepositories = new HashMap<String, PomRepository>();

	public void addRepository(PomRepository pomRepository) {
		this.pomRepositories.put(pomRepository.getUID(), pomRepository);
	}

	public void addRepositories(Set<PomRepository> repositories) {
		for (PomRepository repository : repositories) {
			if (!this.pomRepositories.containsKey(repository.getUID())) {
				this.pomRepositories.put(repository.getUID(), repository);
			}
		}
	}

	public Map<String, PomRepository> getPomRepositories() {
		return Collections.unmodifiableMap(pomRepositories);
	}

	@Override
	public boolean equals(Object obj) {
		PomRepositoriesSet other = (PomRepositoriesSet) obj;
		return mapEquals(this.pomRepositories, other.pomRepositories);
	}

	@SuppressWarnings("unchecked")
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