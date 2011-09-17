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
package org.org.eclipse.dws.ui.internal.views.actions;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.eclipse.dws.core.internal.bridges.LibraryPackModelPersistence;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;
import org.org.eclipse.dws.ui.internal.views.LibraryPacksView;
import org.org.model.RootModelItem;

/**
 * The Class RemoveLibraryPackAction.
 * 
 * @author pagregoire
 */
public class RemoveLibraryPackAction extends AbstractSimpleAction {

	/** The resolver. */
	private IActionResolver resolver;

	/** The action host. */
	private IActionHost actionHost;

	/**
	 * Instantiates a new removes the library pack action.
	 * 
	 * @param actionHost
	 *            the action host
	 */
	public RemoveLibraryPackAction(IActionHost actionHost) {
		this.actionHost = actionHost;
	}

	/**
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		boolean result = false;
		if (resolver == null) {
			result = false;
		} else {
			result = resolver.isEnabled();
		}
		return result;
	}

	/**
	 * Sets the resolver.
	 * 
	 * @param resolver
	 *            the new resolver
	 */
	public void setResolver(IActionResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object next = it.next();
			if (next instanceof LibraryPack) {
				LibraryPack libraryPack = (LibraryPack) next;
				RootModelItem.<LibraryPack> getInstance(ModelConstants.LIBRARYPACKS_ROOT).removeChild(libraryPack.getUID());
			}
		}
		LibraryPackModelPersistence.saveLibraryPackInfo();
		LibraryPacksView.refreshViewer();
	}
}