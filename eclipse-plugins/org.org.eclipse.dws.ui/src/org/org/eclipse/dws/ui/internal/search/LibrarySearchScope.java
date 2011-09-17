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
package org.org.eclipse.dws.ui.internal.search;

import org.eclipse.core.runtime.Assert;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class LibrarySearchScope.
 */
public class LibrarySearchScope {

	/** The description. */
	private String description;

	/** The crawledRepositories. */
	private final CrawledRepository[] crawledRepositories;

	/**
	 * Returns a workspace scope.
	 * 
	 * @return the library search scope
	 */
	public static LibrarySearchScope newSearchScope() {
		return new LibrarySearchScope(LibrarySearchMessages.SearchScope_workspace, getRepositoriesFromModel());
	}

	/**
	 * Gets the crawledRepositories from model.
	 * 
	 * @return the crawledRepositories from model
	 */
	private static CrawledRepository[] getRepositoriesFromModel() {
		CrawledRepository[] result = new CrawledRepository[RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).getChildren().size()];
		int i = 0;
		for (CrawledRepository element : RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).getChildren()) {
			result[i] = element;
		}
		return result;
	}

	/**
	 * Instantiates a new library search scope.
	 * 
	 * @param description
	 *            the description
	 * @param crawledRepositories
	 *            the crawledRepositories
	 */
	private LibrarySearchScope(String description, CrawledRepository[] repositories) {
		Assert.isNotNull(description);
		this.description = description;
		this.crawledRepositories = repositories;
	}

	/**
	 * Returns the description of the scope.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(description);
		text.append(" [");
		for (int i = 0; i < crawledRepositories.length; i++) {
			text.append(crawledRepositories[i]);
			if (i < (crawledRepositories.length - 1)) {
				text.append(", ");
			}
		}
		text.append(']');
		return text.toString();
	}

	/**
	 * Gets the crawledRepositories.
	 * 
	 * @return the crawledRepositories
	 */
	public CrawledRepository[] getRepositories() {
		return crawledRepositories;
	}

}
