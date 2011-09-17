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
package org.org.eclipse.cheatsheet.catalog.internal.views.actions;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForAllElementsAction;
import org.org.model.IModelItem;

public class LockSwitchAction extends EnabledForAllElementsAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public LockSwitchAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	@SuppressWarnings("unchecked")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		Set<CheatSheetCatalog> catalogs = new LinkedHashSet<CheatSheetCatalog>();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			IModelItem modelItem = (IModelItem) it.next();
			try {
				if (modelItem instanceof CheatSheetCatalog) {
					catalogs.add((CheatSheetCatalog) modelItem);
				} else if (modelItem instanceof CheatSheetCategory) {
					catalogs.add(((CheatSheetCategory) modelItem).getParent());
				} else if (modelItem instanceof CheatSheetReference) {
					catalogs.add(((CheatSheetReference) modelItem).getParent().getParent());
				}
			} catch (Exception e) {
			}
		}
		for (CheatSheetCatalog catalog : catalogs) {
			catalog.setReadOnly(!catalog.getReadOnly());
		}
		this.cheatSheetCatalogView.getViewer().refresh(true);
	}
}