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
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Interface IXmlRepositoriesBinder.
 */
public interface IXmlRepositoriesBinder {

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
	public static final String REPOSITORY_TAG = "repository";

	/** The Constant REPOSITORY_TAG_BASEURL_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_BASEURL_ATTRIBUTE = "baseUrl";

	/** The Constant REPOSITORY_TAG_ENTRYPATTERN_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_ENTRYPATTERN_ATTRIBUTE = "entryPattern";

	/** The Constant REPOSITORY_TAG_GROUPFILTERS_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_GROUPFILTERS_ATTRIBUTE = "groupFilters";

	/** The Constant REPOSITORY_TAG_ID_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_ID_ATTRIBUTE = "id";

	/** The Constant REPOSITORY_TAG_PARENTPATTERN_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_PARENTPATTERN_ATTRIBUTE = "parentPattern";

	/** The Constant REPOSITORY_TAG_FILEPATTERN_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_FILEPATTERN_ATTRIBUTE = "filePattern";

	/** The Constant REPOSITORY_TAG_DIRECTORYPATTERN_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_DIRECTORYPATTERN_ATTRIBUTE = "directoryPattern";

	/** The Constant REPOSITORY_TAG_PROXYHOST_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_PROXYHOST_ATTRIBUTE = "proxyHost";

	/** The Constant REPOSITORY_TAG_PROXYPORT_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_PROXYPORT_ATTRIBUTE = "proxyPort";

	/** The Constant REPOSITORY_TAG_TYPE_ATTRIBUTE. */
	public static final String REPOSITORY_TAG_TYPE_ATTRIBUTE = "type";

	/** The Constant REPOSITORY_TAG_TYPE_ATTRIBUTE_FILESYSTEM_VALUE. */
	public static final String REPOSITORY_TAG_TYPE_ATTRIBUTE_FILESYSTEM_VALUE = "filesystem";

	/** The Constant REPOSITORY_TAG_TYPE_ATTRIBUTE_HTTP_VALUE. */
	public static final String REPOSITORY_TAG_TYPE_ATTRIBUTE_HTTP_VALUE = "http";

	/** The Constant LIBRARY_PACKS_TAG. */
	public static final String REPOSITORIES_TAG = "repositories";

	/**
	 * Parses the xml repositories.
	 * 
	 * @param inputStream
	 *            the input stream
	 * 
	 * @return the list< repository>
	 */
	public abstract List<CrawledRepository> parseXmlRepositories(InputStream inputStream) throws BindingException;

	/**
	 * Parses the xml repositories.
	 * 
	 * @param input
	 *            the input
	 * 
	 * @return the list< repository>
	 */
	public abstract List<CrawledRepository> parseXmlRepositories(String input) throws BindingException;

	/**
	 * Parses the xml definition.
	 * 
	 * @param inputStream
	 *            the input stream
	 * 
	 * @return the repository
	 */
	public abstract CrawledRepository parseXmlDefinition(InputStream inputStream) throws BindingException;

	/**
	 * Parses the xml definition.
	 * 
	 * @param input
	 *            the input
	 * 
	 * @return the repository
	 */
	public abstract CrawledRepository parseXmlDefinition(String input) throws BindingException;

	/**
	 * To xml repositories.
	 * 
	 * @param crawledRepositories
	 *            the repositories
	 * 
	 * @return the string
	 */
	public abstract String toXmlRepositories(List<CrawledRepository> crawledRepositories) throws BindingException;

	/**
	 * To xml repositories.
	 * 
	 * @param crawledRepositories
	 *            the repositories
	 * @param outputStream
	 *            the output stream
	 */
	public abstract void toXmlRepositories(List<CrawledRepository> crawledRepositories, OutputStream outputStream) throws BindingException;

	/**
	 * To xml definition.
	 * 
	 * @param crawledRepository
	 *            the repository
	 * 
	 * @return the string
	 */
	public abstract String toXmlDefinition(CrawledRepository crawledRepository) throws BindingException;

	/**
	 * To xml definition.
	 * 
	 * @param crawledRepository
	 *            the repository
	 * @param outputStream
	 *            the output stream
	 */
	public abstract void toXmlDefinition(CrawledRepository crawledRepository, OutputStream outputStream) throws BindingException;

}