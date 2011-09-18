package org.org.eclipse.dws.core.internal.model;

import java.util.concurrent.atomic.AtomicBoolean;

import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class ArtifactVersionWrapper.
 */
public class ArtifactVersionWrapper {

	/** The library id. */
	private final String libraryId;

	/** The artifact version. */
	private final ArtifactVersion artifactVersion;

	/** The exact match. */
	private final Boolean exactMatch;

	/** The selected. */
	private AtomicBoolean selected;

	/**
	 * Instantiates a new artifact version wrapper.
	 * 
	 * @param artifactVersion
	 *            the artifact version
	 * @param exactMatch
	 *            the exact match
	 * @param libraryId
	 *            the library id
	 */
	public ArtifactVersionWrapper(ArtifactVersion artifactVersion, Boolean exactMatch, String libraryId) {
		this.artifactVersion = artifactVersion;
		this.exactMatch = exactMatch;
		this.libraryId = libraryId;
		if (exactMatch) {
			selected = new AtomicBoolean(true);
		} else {
			selected = new AtomicBoolean(false);
		}
	}

	/**
	 * Gets the artifact version.
	 * 
	 * @return the artifact version
	 */
	public ArtifactVersion getArtifactVersion() {
		return artifactVersion;
	}

	/**
	 * Gets the exact match.
	 * 
	 * @return the exact match
	 */
	public Boolean getExactMatch() {
		return exactMatch;
	}

	/**
	 * Checks if is selected.
	 * 
	 * @return the boolean
	 */
	public Boolean isSelected() {
		return selected.get();
	}

	/**
	 * Invert selection.
	 */
	public void invertSelection() {
		selected.getAndSet(!selected.get());
	}

	/**
	 * Gets the uID.
	 * 
	 * @return the uID
	 */
	public String getUID() {
		return libraryId + artifactVersion.getUID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append((exactMatch ? "[EXACT_MATCH]" : "") + (selected.get() ? "[SELECTED]" : "") + artifactVersion.getId());
		return stringBuilder.toString();
	}
}