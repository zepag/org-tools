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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class AbstractLibraryQuery.
 */
public abstract class AbstractLibraryQuery implements ISearchQuery {

	/** The scope. */
	private LibrarySearchScope scope;

	/** The pattern. */
	private String pattern;

	/** The result. */
	private ISearchResult result;

	/**
	 * Instantiates a new abstract library query.
	 * 
	 * @param scope
	 *            the scope
	 * @param pattern
	 *            the pattern
	 */
	public AbstractLibraryQuery(LibrarySearchScope scope, String pattern) {
		Assert.isNotNull(scope);
		this.scope = scope;
		this.pattern = pattern;
	}

	/**
	 * Gets the scope.
	 * 
	 * @return the scope
	 */
	public LibrarySearchScope getScope() {
		return scope;
	}

	/**
	 * Gets the pattern.
	 * 
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
	 */
	/**
	 * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
	 */
	public final ISearchResult getSearchResult() {
		if (result == null) {
			result = new LibrarySearchResult(this);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#canRerun()
	 */
	/**
	 * @see org.eclipse.search.ui.ISearchQuery#canRerun()
	 */
	public boolean canRerun() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
	 */
	/**
	 * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
	 */
	public boolean canRunInBackground() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final IStatus run(IProgressMonitor monitor) {
		final LibrarySearchResult result = (LibrarySearchResult) getSearchResult();
		result.removeAll();
		CrawledRepository[] repositories = scope.getRepositories();
		final IProgressMonitor fMonitor = monitor;
		for (int i = 0; !monitor.isCanceled() && i < repositories.length; i++) {
			CrawledRepository crawledRepository = repositories[i];
			IModelItemVisitor visitor = new IModelItemVisitor() {
				@SuppressWarnings("unchecked")
				public boolean visit(IModelItem modelItem) {
					if (doesMatch(modelItem, pattern, fMonitor)) {
						Match match = new Match(modelItem, -1, -1);
						result.addMatch(match);
					}
					return true;
				}
			};
			crawledRepository.accept(visitor);
		}
		Object[] args = new Object[] { Integer.valueOf(result.getMatchCount()) };
		String message = MessageFormat.format(LibrarySearchMessages.SearchQuery_status, args);
		return new Status(IStatus.OK, DWSUIPlugin.class.getName(), 0, message, null);
	}

	/**
	 * Returns <code>true</code> if given <code>IModelElement</code> matches this query.
	 * 
	 * @param element
	 *            the element
	 * @param pattern
	 *            the pattern
	 * @param monitor
	 *            the monitor
	 * 
	 * @return true, if does match
	 */
	@SuppressWarnings("unchecked")
	protected abstract boolean doesMatch(IModelItem element, String pattern, IProgressMonitor monitor);
}