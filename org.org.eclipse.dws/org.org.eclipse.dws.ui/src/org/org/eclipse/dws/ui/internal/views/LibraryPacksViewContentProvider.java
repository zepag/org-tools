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
package org.org.eclipse.dws.ui.internal.views;

import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;
import org.org.eclipse.dws.core.internal.bridges.LibraryPackModelPersistence;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;
import org.org.model.IModelItem;
import org.org.model.IModelItemListener;
import org.org.model.RootModelItem;

/**
 * The Class MavenRepositoriesViewContentProvider.
 */
class LibraryPacksViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	/** The invisible root. */
	private RootModelItem<LibraryPack> invisibleRoot;

	/** The view site. */
	private IViewSite viewSite;

	/** The listener. */
	private IModelItemListener listener;

	/**
	 * Instantiates a new maven repositories view content provider.
	 * 
	 * @param viewSite
	 *            the view site
	 */
	public LibraryPacksViewContentProvider(IViewSite viewSite) {
		this.viewSite = viewSite;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		invisibleRoot.removeListener(listener);
		invisibleRoot = null;
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		// if parent is the viewSite, get the root element
		if (parent.equals(this.viewSite)) {
			if (invisibleRoot == null) {
				initialize();
			}
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		if (!RootModelItem.isInstanciated(ModelConstants.LIBRARYPACKS_ROOT)) {
			LibraryPackModelPersistence.loadLibraryPackInfo();
		}
		invisibleRoot = RootModelItem.<LibraryPack> getInstance(ModelConstants.LIBRARYPACKS_ROOT);
		LibraryPackModelPersistence.saveLibraryPackInfo();
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	public Object getParent(Object child) {
		if (child instanceof IModelItem) {
			return ((IModelItem) child).getParent();
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object[] getChildren(Object parent) {
		if (parent instanceof IModelItem) {
			Set<IModelItem> children = ((IModelItem) parent).getChildren();
			return children.toArray(new IModelItem[children.size()]);
		}
		return new Object[0];
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	public boolean hasChildren(Object parent) {
		if (parent instanceof IModelItem)
			return ((IModelItem) parent).hasChildren();
		return false;
	}
}