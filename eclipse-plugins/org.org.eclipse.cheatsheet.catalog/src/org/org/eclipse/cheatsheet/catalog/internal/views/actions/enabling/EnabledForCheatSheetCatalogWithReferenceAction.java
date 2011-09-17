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
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.core.utils.platform.views.actions.ResolvedAction;
import org.org.model.IModelItem;

public abstract class EnabledForCheatSheetCatalogWithReferenceAction extends ResolvedAction<CheatSheetCatalogView> {

	public EnabledForCheatSheetCatalogWithReferenceAction(CheatSheetCatalogView actionHost) {
		super(actionHost);
	}

	@SuppressWarnings("unchecked")
	public boolean isEnabled() {
		IStructuredSelection selection = (IStructuredSelection) super.getActionHost().getViewer().getSelection();
		boolean result = false;
		for (Iterator it = selection.iterator(); it.hasNext();) {
			IModelItem modelItem = (IModelItem) it.next();
			if (modelItem instanceof CheatSheetCatalog && ((CheatSheetCatalog) modelItem).getReference() != null) {
				result = true;
				break;
			} else {
				result = false;
			}
		}
		return result;
	}
}