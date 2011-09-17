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

package org.org.eclipse.dws.core.internal.xml;

import java.io.InputStream;
import java.util.Set;

import org.org.eclipse.core.utils.platform.binding.BindingException;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomDependency;


/**
 * The Interface IXmlPomFileBinder.
 */
public interface IXmlPomFileBinder {
	
	/** The Constant XML_VERSION_1_0. */
	public static final String XML_VERSION_1_0 = "1.0";
	
	/** The Constant UTF_8. */
	public static final String UTF_8 = "UTF-8";
	
	/** The Constant ARTIFACT_TAG. */
	public static final String ARTIFACT_TAG = "pom";

	/** The Constant PROJECT_TAG. */
	static final String PROJECT_TAG = "project";
	
	/** The Constant DEPENDENCIES_TAG. */
	static final String DEPENDENCIES_TAG = "dependencies";
	
	/** The Constant DEPENDENCY_TAG. */
	static final String DEPENDENCY_TAG = "dependency";
	
	/** The Constant GROUPID_TAG. */
	static final String GROUPID_TAG = "groupId";
	
	/** The Constant ARTIFACTID_TAG. */
	static final String ARTIFACTID_TAG = "artifactId";
	
	/** The Constant VERSION_TAG. */
	static final String VERSION_TAG = "version";
	
	/** The Constant CLASSIFIER_TAG. */
	static final String CLASSIFIER_TAG = "classifier";
	
	/** The Constant OPTIONAL_TAG. */
	static final String OPTIONAL_TAG = "optional";
	
	/** The Constant SCOPE_TAG. */
	static final String SCOPE_TAG = "scope";
	
	/** The Constant PARENT_TAG. */
	static final String PARENT_TAG = "parent";
	
	/** The Constant RELATIVEPATH_TAG. */
	static final String RELATIVEPATH_TAG = "relativePath";
	
	/** The Constant DESCRIPTION_TAG. */
	static final String DESCRIPTION_TAG = "description";
	
	/** The Constant URL_TAG. */
	static final String URL_TAG = "url";
	
	/** The Constant NAME_TAG. */
	static final String NAME_TAG = "name";
	
	/** The Constant LIBRARY_PACK_TAG. */
	static final String REPOSITORY_TAG = "repository";
	
	/** The Constant SNAPSHOT_REPOSITORY_TAG. */
	static final String SNAPSHOT_REPOSITORY_TAG = "snapshot-repository";
	
	/** The Constant ID_TAG. */
	static final String ID_TAG = "id";
	
	/** The Constant SYSTEMPATH_TAG. */
	static final String SYSTEMPATH_TAG = "systemPath";
	
	/** The Constant PROPERTIES_TAG. */
	static final String PROPERTIES_TAG = "properties";
	
	/** The Constant PACKAGING_TAG. */
	static final String PACKAGING_TAG = "packaging";

	/**
	 * Parses the pom file.
	 * 
	 * @param inputStream the input stream
	 * 
	 * @return the parsed pom description
	 */
	public abstract Pom parsePomFile(InputStream inputStream) throws BindingException;

	/**
	 * Parses the pom file.
	 * 
	 * @param input the input
	 * 
	 * @return the parsed pom description
	 */
	public abstract Pom parsePomFile(String input) throws BindingException;

	/**
	 * Update pom.
	 * 
	 * @param pomStream the pom stream
	 * @param pomDependencies the pom dependencies
	 * @param pomEncoding the pom encoding
	 * 
	 * @return the string
	 */
	public abstract String updatePom(InputStream pomStream, Set<PomDependency> pomDependencies, String pomEncoding) throws BindingException;
}