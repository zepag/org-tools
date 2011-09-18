/*******************************************************************************
 * Copyright (c) 2008 Pierre-Antoine Grégoire.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pierre-Antoine Grégoire - initial API and implementation
 *******************************************************************************/
package org.org.eclipse.dws.core.internal.model;

import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;


/**
 * The Class ParentPom.
 */
@SuppressWarnings("rawtypes")
public class ParentPom extends AbstractModelItem<IModelItem, IModelItem> {
	
	/** The artifact id. */
	private String artifactId;

	/** The group id. */
	private String groupId;

	/** The version. */
	private String version;

	/** The relative path. */
	private String relativePath;

	/**
	 * Gets the artifact id.
	 * 
	 * @return the artifact id
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * Sets the artifact id.
	 * 
	 * @param artifactId the new artifact id
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * Gets the group id.
	 * 
	 * @return the group id
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Sets the group id.
	 * 
	 * @param groupId the new group id
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Gets the relative path.
	 * 
	 * @return the relative path
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * Sets the relative path.
	 * 
	 * @param relativePath the new relative path
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version.
	 * 
	 * @param version the new version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see org.org.model.AbstractModelItem#compareTo(org.org.model.IModelItem)
	 */
	/**
	 * @see org.org.model.AbstractModelItem#compareTo(org.org.model.IModelItem)
	 */
	@Override
	public int compareTo(IModelItem o) {
		ParentPom parentPom = (ParentPom) o;
		return String.CASE_INSENSITIVE_ORDER.compare(groupId + artifactId + version, parentPom.groupId + parentPom.artifactId + parentPom.version);
	}

	/* (non-Javadoc)
	 * @see org.org.model.AbstractModelItem#getUID()
	 */
	/**
	 * @see org.org.model.AbstractModelItem#getUID()
	 */
	@Override
	public String getUID() {
		return this.getGroupId() + ":" + this.getArtifactId() + ":" + this.getVersion() + ":" + this.getRelativePath();
	}

	/* (non-Javadoc)
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	/**
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(this.getUID());
	}
}
