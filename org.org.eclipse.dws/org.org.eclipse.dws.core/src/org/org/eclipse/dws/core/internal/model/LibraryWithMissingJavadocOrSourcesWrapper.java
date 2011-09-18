package org.org.eclipse.dws.core.internal.model;

import java.util.Set;

import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * The Class LibraryWithMissingJavadocOrSourcesWrapper.
 */
public class LibraryWithMissingJavadocOrSourcesWrapper {

	/** The library path. */
	private final String libraryPath;

	/** The library id. */
	private final String libraryId;

	/** The artifact version wrappers. */
	private final Set<ArtifactVersionWrapper> artifactVersionWrappers;

	/** The misses javadoc. */
	private final Boolean missesJavadoc;

	/** The misses sources. */
	private final Boolean missesSources;

	/** The package fragment root. */
	private final IPackageFragmentRoot packageFragmentRoot;

	/**
	 * Instantiates a new library with missing javadoc or sources wrapper.
	 * 
	 * @param libraryPath
	 *            the library path
	 * @param libraryId
	 *            the library id
	 * @param artifactVersionWrappers
	 *            the artifact version wrappers
	 * @param missesJavadoc
	 *            the misses javadoc
	 * @param missesSources
	 *            the misses sources
	 * @param packageFragmentRoot
	 *            the package fragment root
	 */
	public LibraryWithMissingJavadocOrSourcesWrapper(String libraryPath, String libraryId, Set<ArtifactVersionWrapper> artifactVersionWrappers, Boolean missesJavadoc, Boolean missesSources, IPackageFragmentRoot packageFragmentRoot) {
		this.libraryPath = libraryPath;
		this.libraryId = libraryId;
		this.artifactVersionWrappers = artifactVersionWrappers;
		this.missesJavadoc = missesJavadoc;
		this.missesSources = missesSources;
		this.packageFragmentRoot = packageFragmentRoot;
	}

	/**
	 * Gets the artifact version wrappers.
	 * 
	 * @return the artifact version wrappers
	 */
	public Set<ArtifactVersionWrapper> getArtifactVersionWrappers() {
		return artifactVersionWrappers;
	}

	/**
	 * Gets the package fragment root.
	 * 
	 * @return the package fragment root
	 */
	public IPackageFragmentRoot getPackageFragmentRoot() {
		return packageFragmentRoot;
	}

	/**
	 * Gets the library id.
	 * 
	 * @return the library id
	 */
	public String getLibraryId() {
		return libraryId;
	}

	/**
	 * Gets the library path.
	 * 
	 * @return the library path
	 */
	public String getLibraryPath() {
		return libraryPath;
	}

	/**
	 * Gets the misses javadoc.
	 * 
	 * @return the misses javadoc
	 */
	public Boolean getMissesJavadoc() {
		return missesJavadoc;
	}

	/**
	 * Gets the misses sources.
	 * 
	 * @return the misses sources
	 */
	public Boolean getMissesSources() {
		return missesSources;
	}

}