package org.org.eclipse.dws.ui.internal.views.actions;

import java.util.HashSet;
import java.util.Set;

import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.GroupsHolder;

/**
 * The Class ArtifactsRetrievalVisitor.
 */
class ArtifactsRetrievalVisitor implements IModelItemVisitor {

	/** The artifact versions. */
	private Set<ArtifactVersion> artifactVersions = new HashSet<ArtifactVersion>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
	 */
	/**
	 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(IModelItem modelItem) {
		boolean result = false;
		if (modelItem instanceof Group || modelItem instanceof GroupsHolder || modelItem instanceof Artifact) {
			result = true;
		} else if (modelItem instanceof ArtifactVersion) {
			artifactVersions.add((ArtifactVersion) modelItem);
		}
		return result;
	}

	/**
	 * Gets the artifact versions.
	 * 
	 * @return the artifact versions
	 */
	public Set<ArtifactVersion> getArtifactVersions() {
		return artifactVersions;
	}

}