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
import org.org.repository.crawler.maven2.model.ArtifactVersion;


/**
 * The Class PomCreationDescription.
 */
@SuppressWarnings("rawtypes")
public class PomCreationDescription extends AbstractModelItem<IModelItem,ArtifactVersion> {

	/** The group id. */
	private String groupId;

	/** The artifact id. */
	private String artifactId;

	/** The version. */
	private String version;

	/** The packaging. */
	private String packaging;
	
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
	 * @param artifactId the artifact id
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
	 * @param groupId the group id
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
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
	 * @param version the version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Compare to.
	 * 
	 * @param o the o
	 * 
	 * @return the int
	 * 
	 * @see java.lang.Comparable
	 */
	@Override
	public int compareTo(IModelItem o) {
		PomCreationDescription pomCreationDescription = (PomCreationDescription) o;
		return String.CASE_INSENSITIVE_ORDER.compare(groupId + artifactId + version, pomCreationDescription.groupId + pomCreationDescription.artifactId + pomCreationDescription.version);
	}

	
	/**
	 * @see org.org.model.AbstractModelItem#getUID()
	 */
	@Override
	public String getUID() {
		return this.getGroupId() + ":" + this.getArtifactId() + ":" + this.getVersion() + ":" + this.getPackaging();
	}

	/**
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(this.getGroupId() + ":" + this.getArtifactId() + ":" + this.getVersion() + ":" + this.getPackaging());
	}

	/**
	 * Gets the packaging.
	 * 
	 * @return the packaging
	 */
	public String getPackaging() {
		return packaging;
	}

	/**
	 * Sets the packaging.
	 * 
	 * @param archiveType the new packaging
	 */
	public void setPackaging(String archiveType) {
		this.packaging = archiveType;
	}

}
