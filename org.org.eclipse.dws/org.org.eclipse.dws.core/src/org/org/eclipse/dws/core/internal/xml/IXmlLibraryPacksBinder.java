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
import java.io.OutputStream;
import java.util.List;

import org.org.eclipse.core.utils.platform.binding.BindingException;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;

/**
 * The Interface IXmlRepositoriesBinder.
 */
public interface IXmlLibraryPacksBinder {

	/** The Constant XML_VERSION_1_0. */
	public static final String XML_VERSION_1_0 = "1.0";

	/** The Constant UTF_8. */
	public static final String UTF_8 = "UTF-8";

	/** The Constant ARTIFACT_TAG. */
	public static final String ARTIFACT_TAG = "artifact";

	/** The Constant ARTIFACT_TAG_ID_ATTRIBUTE. */
	public static final String ARTIFACT_TAG_ID_ATTRIBUTE = "id";

	/** The Constant ARTIFACTVERSION_TAG. */
	public static final String ARTIFACTVERSION_TAG = "artifact-version";

	/** The Constant ARTIFACTVERSION_TAG_ID_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_ID_ATTRIBUTE = "id";

	/** The Constant ARTIFACTVERSION_TAG_TYPE_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_TYPE_ATTRIBUTE = "type";

	/** The Constant ARTIFACTVERSION_TAG_POM_URL_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_POM_URL_ATTRIBUTE = "pomUrl";

	/** The Constant ARTIFACTVERSION_TAG_JAVADOC_URL_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_JAVADOC_URL_ATTRIBUTE = "javadoc";

	/** The Constant ARTIFACTVERSION_TAG_TARGETS_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_TARGETS_ATTRIBUTE = "targets";

	/** The Constant ARTIFACTVERSION_TAG_SOURCES_URL_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_SOURCES_URL_ATTRIBUTE = "sources";

	/** The Constant ARTIFACTVERSION_TAG_URL_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_URL_ATTRIBUTE = "url";

	/** The Constant ARTIFACTVERSION_TAG_VERSION_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_VERSION_ATTRIBUTE = "version";

	/** The Constant ARTIFACTVERSION_TAG_CLASSIFIER_ATTRIBUTE. */
	public static final String ARTIFACTVERSION_TAG_CLASSIFIER_ATTRIBUTE = "classifier";

	/** The Constant GROUP_TAG. */
	public static final String GROUP_TAG = "group";

	/** The Constant GROUP_TAG_NAME_ATTRIBUTE. */
	public static final String GROUP_TAG_NAME_ATTRIBUTE = "name";

	/** The Constant LIBRARY_PACK_TAG. */
	public static final String LIBRARY_PACK_TAG = "library-pack";

	/** The Constant LIBRARY_PACK_TAG. */
	public static final String DESCRIPTION_TAG = "description";

	/** The Constant LIBRARY_PACKS_TAG. */
	public static final String LIBRARY_PACKS_TAG = "library-packs";
	/** The Constant LIBRARY_PACK_LABEL_ATTRIBUTE. */
	public static final String LIBRARY_PACK_LABEL_ATTRIBUTE = "label";

	/**
	 * Parses the xml repositories.
	 * 
	 * @param inputStream
	 *            the input stream
	 * 
	 * @return the list< repository>
	 */
	public abstract List<LibraryPack> parseXmlLibraryPacks(InputStream inputStream) throws BindingException;

	/**
	 * Parses the xml repositories.
	 * 
	 * @param input
	 *            the input
	 * 
	 * @return the list< repository>
	 */
	public abstract List<LibraryPack> parseXmlLibraryPacks(String input) throws BindingException;

	/**
	 * To xml repositories.
	 * 
	 * @param crawledRepositories
	 *            the repositories
	 * 
	 * @return the string
	 */
	public abstract String toXmlLibraryPacks(List<LibraryPack> crawledRepositories) throws BindingException;

	/**
	 * To xml repositories.
	 * 
	 * @param linkedList
	 *            the repositories
	 * @param outputStream
	 *            the output stream
	 */
	public abstract void toXmlLibraryPacks(List<LibraryPack> linkedList, OutputStream outputStream) throws BindingException;

}