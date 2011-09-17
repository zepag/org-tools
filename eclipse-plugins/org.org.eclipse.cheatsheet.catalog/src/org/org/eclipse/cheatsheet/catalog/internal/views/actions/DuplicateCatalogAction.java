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

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.org.eclipse.cheatsheet.catalog.internal.dialogs.CheatSheetCatalogNameDialog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForCheatSheetCatalogAction;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.model.RootModelItem;

public class DuplicateCatalogAction extends EnabledForCheatSheetCatalogAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public DuplicateCatalogAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		IStructuredSelection structuredSelection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		CheatSheetCatalog catalog = (CheatSheetCatalog) structuredSelection.getFirstElement();
		RootModelItem<CheatSheetCatalog> root = catalog.getParent();
		CheatSheetCatalogNameDialog inputDialog = new CheatSheetCatalogNameDialog(this.cheatSheetCatalogView.getViewSite().getShell());
		inputDialog.initEdit(catalog.getName());
		inputDialog.setValidator(new CatalogsAwareFieldsValidator(root) {

			@SuppressWarnings("unchecked")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				String name = (String) fieldValueHolders.get(CheatSheetCatalogNameDialog.NAME_FIELD).getValue();
				if (name.trim().length() == 0) {
					validationResult.append("Name should not be empty\n");
				} else if (!(name).matches("[\\p{L}\\- 0-9]+")) {
					validationResult.append("Name is not valid\n");
				} else if (getRoot().hasChild(name)) {
					validationResult.append("Catalog name must not already exist in local catalog\n");
				}
				return validationResult;
			}

		});
		if (inputDialog.open() == Window.OK) {
			catalog = catalog.duplicate(inputDialog.getName(), catalog.getProvider());
			root.addChild(catalog);
			this.cheatSheetCatalogView.refreshViewerKeepFolding();
			ModelPersistence.saveCheatSheetCatalogs();
		}
	}
}