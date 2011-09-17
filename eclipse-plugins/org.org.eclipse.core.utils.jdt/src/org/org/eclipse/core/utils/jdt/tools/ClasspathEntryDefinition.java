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
package org.org.eclipse.core.utils.jdt.tools;

import org.eclipse.core.runtime.IPath;

public class ClasspathEntryDefinition {
	public enum EntryType {
		/**
		 * Classpath entry's path is defined with a variable.
		 */
		VARIABLE,
		/**
		 * Classpath entry's path is entirely defined with a path.
		 */
		PATH
	}

	private IPath entryPath;
	private IPath sourcesPath;
	private IPath javadocPath;
	private String classpathEntry;
	private EntryType entryType;

	public ClasspathEntryDefinition(IPath entryPath, IPath sourcesPath, IPath javadocPath, EntryType entryType) {
		this.entryPath = entryPath;
		this.sourcesPath = sourcesPath;
		this.javadocPath = javadocPath;
		this.entryType = entryType;
	}

	public IPath getEntryPath() {
		return entryPath;
	}

	public IPath getSourcesPath() {
		return sourcesPath;
	}

	public IPath getJavadocPath() {
		return javadocPath;
	}

	public EntryType getEntryType() {
		return entryType;
	}

	public String getClasspathEntry() {
		return classpathEntry;
	}

	public void setClasspathEntry(String classpathEntry) {
		this.classpathEntry = classpathEntry;
	}
}