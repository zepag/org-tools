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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.core.utils.platform.views.actions.ResolvedAction;

public abstract class EnabledForWritableCheatSheetCategoryAction extends ResolvedAction<CheatSheetCatalogView> {

	public EnabledForWritableCheatSheetCategoryAction(CheatSheetCatalogView actionHost) {
		super(actionHost);
	}

	@SuppressWarnings("unchecked")
	public boolean isEnabled() {
		IStructuredSelection selection = (IStructuredSelection) super.getActionHost().getViewer().getSelection();
		boolean result = false;
		Object item = selection.getFirstElement();
		try {
			if (item != null && item instanceof CheatSheetCategory && !((CheatSheetCategory) item).getParent().getReadOnly()) {
				result = true;
			}
		} catch (Exception e) {
			result = false;
		}
		return result;
	}
}