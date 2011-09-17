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

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.ArtifactVersion;


/**
 * The Class LibraryNameQuery.
 */
public class LibraryNameQuery extends AbstractLibraryQuery {

	/**
	 * Instantiates a new library name query.
	 * 
	 * @param scope the scope
	 * @param pattern the pattern
	 */
	public LibraryNameQuery(LibrarySearchScope scope, String pattern) {
		super(scope, pattern);
	}

	/**
	 * @see org.eclipse.search.ui.ISearchQuery#getLabel()
	 */
	public String getLabel() {
		Object[] args = new Object[] { getPattern(), getScope().getDescription() };
		return MessageFormat.format(LibrarySearchMessages.SearchQuery_searchFor_name, args);
	}

	/**
	 * @see org.org.eclipse.dws.ui.internal.search.AbstractLibraryQuery#doesMatch(org.org.model.IModelItem, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected boolean doesMatch(IModelItem element, String pattern, IProgressMonitor monitor) {
		if (element instanceof ArtifactVersion) {
			ArtifactVersion artifactVersion = (ArtifactVersion) element;

			// Compare library name first
			String toMatch = artifactVersion.getId().substring(0, artifactVersion.getId().lastIndexOf("."));
			if (toMatch.contains(pattern)) {
				return true;
			}
		}
		return false;
	}

}
