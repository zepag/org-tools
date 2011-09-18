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
package org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.core.utils.platform.views.actions.ResolvedAction;
import org.org.model.IModelItem;

public abstract class EnabledForRemoveableCheatSheetElementAction extends ResolvedAction<CheatSheetCatalogView> {
	public EnabledForRemoveableCheatSheetElementAction(CheatSheetCatalogView actionHost) {
		super(actionHost);
	}

	
	public boolean isEnabled() {
		IStructuredSelection selection = (IStructuredSelection) super.getActionHost().getViewer().getSelection();
		boolean result = !selection.isEmpty();
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			IModelItem<?,?> modelItem = (IModelItem<?,?>) it.next();
			CheatSheetCatalog cheatSheetCatalog = null;
			try {
				if (modelItem instanceof CheatSheetCatalog) {
					cheatSheetCatalog = (CheatSheetCatalog) modelItem;
				} else if (modelItem instanceof CheatSheetCategory) {
					cheatSheetCatalog = ((CheatSheetCategory) modelItem).getParent();
				} else if (modelItem instanceof CheatSheetReference) {
					cheatSheetCatalog = ((CheatSheetReference) modelItem).getParent().getParent();
				}
			} catch (Exception e) {
				cheatSheetCatalog = null;
			}
			if (cheatSheetCatalog == null || cheatSheetCatalog.getReadOnly()) {
				result = false;
				break;
			}
		}
		return result;
	}
}