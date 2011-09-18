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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.model.IModelItem;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class LibrarySearchContentProvider.
 */
public class LibrarySearchContentProvider implements ITreeContentProvider, IStructuredContentProvider {

	/** The Constant EMPTY. */
	private static final Object[] EMPTY = new Object[] {};

	/** The result. */
	private LibrarySearchResult result;

	/** The viewer. */
	private Viewer viewer;

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof LibrarySearchResult) {
			result = (LibrarySearchResult) inputElement;
		}

		if (result == null) {
			return null;
		}

		// Create list of repositories the library belongs to
		Object[] matches = result.getElements();
		List<IModelItem> repositories = new ArrayList<IModelItem>();
		for (int i = 0; i < matches.length; i++) {
			IModelItem element = (IModelItem) matches[i];
			IModelItem repository = RepositoryModelUtils.getChildForElement(RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT), element);
			if (!repositories.contains(repository)) {
				repositories.add(repository);
			}
		}
		return repositories.toArray(new IModelItem[repositories.size()]);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	public Object[] getChildren(Object parentElement) {
		if (result == null) {
			return EMPTY;
		}
		// Create list of matched element's child elements which belong to
		// given parent element
		Object[] matches = result.getElements();
		List<IModelItem> childs = new ArrayList<IModelItem>();
		for (int i = 0; i < matches.length; i++) {
			IModelItem element = (IModelItem) matches[i];
			IModelItem child = RepositoryModelUtils.getChildForElement((IModelItem) parentElement, element);
			if (child != null && !childs.contains(child)) {
				childs.add(child);
			}
		}
		return childs.toArray(new IModelItem[childs.size()]);
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	public Object getParent(Object element) {
		return ((IModelItem) element).getParent();
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	public boolean hasChildren(Object parent) {
		return ((IModelItem) parent).hasChildren();
	}

	/**
	 * Elements changed.
	 * 
	 * @param objects
	 *            the objects
	 */
	public void elementsChanged(Object[] objects) {
		viewer.refresh();
	}
}
