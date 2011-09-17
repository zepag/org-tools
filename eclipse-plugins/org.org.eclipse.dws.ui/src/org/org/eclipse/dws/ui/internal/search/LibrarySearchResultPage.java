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

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;
import org.eclipse.swt.widgets.Composite;
import org.org.eclipse.dws.ui.internal.views.MavenRepositoriesView;
import org.org.eclipse.dws.ui.internal.views.MavenRepositoriesViewLabelProvider;
import org.org.model.IModelItem;

/**
 * The Class LibrarySearchResultPage.
 */
public class LibrarySearchResultPage extends AbstractTextSearchViewPage {

	/** The result listener. */
	private ISearchResultListener resultListener;

	/** The double click listener. */
	private IDoubleClickListener doubleClickListener;

	/** The provider. */
	private LibrarySearchContentProvider provider;

	/**
	 * Instantiates a new library search result page.
	 */
	public LibrarySearchResultPage() {
		super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		setID(LibrarySearchResultPage.class.getName());
		resultListener = new ISearchResultListener() {
			public void searchResultChanged(SearchResultEvent e) {
				handleSearchResultsChanged(e);
			}
		};
		doubleClickListener = new IDoubleClickListener() {
			@SuppressWarnings("unchecked")
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object obj = selection.getFirstElement();
				if (obj instanceof IModelItem) {
					MavenRepositoriesView.showViewAndFocusOnElement((IModelItem) obj);
				}
			}
		};
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
	 */
	@Override
	protected void configureTableViewer(TableViewer viewer) {
		throw new UnsupportedOperationException("Why do you want a table viewer?");
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new MavenRepositoriesViewLabelProvider());
		LibrarySearchContentProvider provider = new LibrarySearchContentProvider();
		viewer.setContentProvider(provider);
		this.provider = provider;
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#createTableViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected TableViewer createTableViewer(Composite parent) {
		TableViewer viewer = super.createTableViewer(parent);
		viewer.addDoubleClickListener(doubleClickListener);
		return viewer;
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#createTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected TreeViewer createTreeViewer(Composite parent) {
		TreeViewer viewer = super.createTreeViewer(parent);
		viewer.addDoubleClickListener(doubleClickListener);
		return viewer;
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Library Search Result";
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#elementsChanged(java.lang.Object[])
	 */
	@Override
	protected void elementsChanged(Object[] objects) {
		if (provider != null) {
			provider.elementsChanged(objects);
		}
	}

	/**
	 * Handle search results changed.
	 * 
	 * @param e
	 *            the e
	 */
	private synchronized void handleSearchResultsChanged(final SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			// MatchEvent me = (MatchEvent) e;
			// postUpdate(me.getMatches());
		} else if (e instanceof RemoveAllEvent) {
			clear();
		}
		// viewer.refresh();
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#setInput(org.eclipse.search.ui.ISearchResult, java.lang.Object)
	 */
	@Override
	public void setInput(ISearchResult search, Object viewState) {
		super.setInput(search, viewState);
		if (search != null) {
			search.addListener(resultListener);
		}
	}

	/**
	 * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#clear()
	 */
	@Override
	protected void clear() {
		// provider.elementsChanged(new Object[] {});
	}
}
