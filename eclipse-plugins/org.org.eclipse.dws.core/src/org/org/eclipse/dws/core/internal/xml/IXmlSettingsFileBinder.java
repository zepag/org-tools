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
import org.org.eclipse.dws.core.internal.model.Maven2Settings;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Interface IXmlSettingsFileBinder.
 */
public interface IXmlSettingsFileBinder {

	/** The Constant XML_VERSION_1_0. */
	public static final String XML_VERSION_1_0 = "1.0";

	/** The Constant UTF_8. */
	public static final String UTF_8 = "UTF-8";

	public static final String LOCALREPOSITORY_TAG_ID_ATTRIBUTE = "localRepository";


	/**
	 * Parses the xml repositories.
	 * 
	 * @param inputStream
	 *            the input stream
	 * 
	 * @return the list< repository>
	 */
	public abstract Maven2Settings parseXmlRepositories(InputStream inputStream) throws BindingException;

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