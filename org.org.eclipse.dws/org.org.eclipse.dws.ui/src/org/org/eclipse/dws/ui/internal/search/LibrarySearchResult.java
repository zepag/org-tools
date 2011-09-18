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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.eclipse.dws.ui.internal.images.PluginImages;

/**
 * The Class LibrarySearchResult.
 */
public class LibrarySearchResult extends AbstractTextSearchResult {

	/** The query. */
	private ISearchQuery query;

	/**
	 * Instantiates a new library search result.
	 * 
	 * @param query
	 *            the query
	 */
	public LibrarySearchResult(ISearchQuery query) {
		this.query = query;
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getEditorMatchAdapter()
	 */
	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		// no editor match adapter
		return null;
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getFileMatchAdapter()
	 */
	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		// no file match adapter
		return null;
	}

	/**
	 * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16);
	}

	/**
	 * @see org.eclipse.search.ui.ISearchResult#getLabel()
	 */
	public String getLabel() {
		return query.getLabel();
	}

	/**
	 * @see org.eclipse.search.ui.ISearchResult#getQuery()
	 */
	public ISearchQuery getQuery() {
		return query;
	}

	/**
	 * @see org.eclipse.search.ui.ISearchResult#getTooltip()
	 */
	public String getTooltip() {
		return getLabel();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Results for " + getQuery().getLabel() + " # = " + getMatchCount();
	}

}