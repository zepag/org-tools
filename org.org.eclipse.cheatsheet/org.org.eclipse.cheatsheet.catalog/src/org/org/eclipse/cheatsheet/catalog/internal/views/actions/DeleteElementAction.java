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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForRemoveableCheatSheetElementAction;
import org.org.model.IModelItem;

public class DeleteElementAction extends EnabledForRemoveableCheatSheetElementAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public DeleteElementAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		IStructuredSelection structuredSelection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		IModelItem<?, ?> modelItem = (IModelItem<?, ?>) structuredSelection.getFirstElement();
		IModelItem<?, ?> parent = modelItem.getParent();
		parent.removeChild(modelItem.getUID());
		this.cheatSheetCatalogView.refreshViewerKeepFolding();
		ModelPersistence.saveCheatSheetCatalogs();
		this.cheatSheetCatalogView.touchTags();
	}
}