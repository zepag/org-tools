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


/**
 * The Class SkippedDependency.
 */
public class SkippedDependency {

	/** The group id. */
	private final String groupId;
	
	/** The artifact id. */
	private final String artifactId;

	/**
	 * Instantiates a new skipped dependency.
	 * 
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 */
	public SkippedDependency(String groupId, String artifactId) {
		super();
		this.groupId = groupId;
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
	 * Gets the artifact id.
	 * 
	 * @return the artifact id
	 */
	public String getArtifactId() {
		return artifactId;
	}
}
