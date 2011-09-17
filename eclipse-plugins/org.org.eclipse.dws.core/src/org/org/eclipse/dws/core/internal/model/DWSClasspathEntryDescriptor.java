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
 * The Class DWSClasspathEntryDescriptor.
 */
@SuppressWarnings("unchecked")
public class DWSClasspathEntryDescriptor extends AbstractModelItem<IModelItem, IModelItem> {

	/** The encoded classpath entry. */
	private String encodedClasspathEntry;

	/** The project name. */
	private String projectName;
	
	/** The path. */
	private String path;

	/** The valid. */
	private Boolean valid;
	
	/**
	 * Sets the valid.
	 * 
	 * @param valid the new valid
	 */
	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	/* (non-Javadoc)
	 * @see org.org.model.AbstractModelItem#getUID()
	 */
	/**
	 * @see org.org.model.AbstractModelItem#getUID()
	 */
	@Override
	public String getUID() {
		return projectName + encodedClasspathEntry;
	}

	/* (non-Javadoc)
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	/**
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(projectName + " : " + encodedClasspathEntry);
	}

	/**
	 * Gets the encoded classpath entry.
	 * 
	 * @return the encoded classpath entry
	 */
	public String getEncodedClasspathEntry() {
		return encodedClasspathEntry;
	}

	/**
	 * Sets the encoded classpath entry.
	 * 
	 * @param encodedClasspathEntry the new encoded classpath entry
	 */
	public void setEncodedClasspathEntry(String encodedClasspathEntry) {
		this.encodedClasspathEntry = encodedClasspathEntry;
	}

	/**
	 * Gets the project name.
	 * 
	 * @return the project name
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * Sets the project name.
	 * 
	 * @param projectName the new project name
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * Gets the path.
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 * 
	 * @param path the new path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Checks if is valid.
	 * 
	 * @return the boolean
	 */
	public Boolean isValid() {
		return valid;
	}

}
