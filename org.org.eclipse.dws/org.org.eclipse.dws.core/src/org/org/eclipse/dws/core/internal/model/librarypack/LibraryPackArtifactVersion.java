package org.org.eclipse.dws.core.internal.model.librarypack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.org.repository.crawler.maven2.model.ArtifactVersion;

public class LibraryPackArtifactVersion extends ArtifactVersion {
	public static enum Target {
		BUNDLED_FOR_RUNTIME, ADDED_TO_PROJECT_CLASSPATH;
	}

	private Set<Target> targets;

	public Set<Target> getTargets() {
		return targets;
	}

	public void setTargets(Target... targets) {
		this.targets = new HashSet<Target>(Arrays.asList(targets));
	}

}