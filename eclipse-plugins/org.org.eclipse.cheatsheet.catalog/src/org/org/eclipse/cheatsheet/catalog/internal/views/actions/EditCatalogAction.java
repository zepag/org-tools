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
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.org.eclipse.cheatsheet.catalog.internal.dialogs.CheatSheetCatalogDialog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalogReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForWritableCheatSheetCatalogAction;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.model.RootModelItem;

public class EditCatalogAction extends EnabledForWritableCheatSheetCatalogAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public EditCatalogAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		IStructuredSelection structuredSelection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		CheatSheetCatalog catalog = (CheatSheetCatalog) structuredSelection.getFirstElement();
		RootModelItem<CheatSheetCatalog> root = catalog.getParent();
		Set<CheatSheetCategory> children = catalog.getChildren();
		CheatSheetCatalogDialog inputDialog = new CheatSheetCatalogDialog(this.cheatSheetCatalogView.getViewSite().getShell());
		inputDialog.initEdit(catalog);
		inputDialog.setValidator(new CatalogsAwareFieldsValidator(root) {

			@SuppressWarnings("unchecked")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				String provider = (String) fieldValueHolders.get(CheatSheetCatalogDialog.PROVIDER_FIELD).getValue();
				if (provider.trim().length() == 0) {
					validationResult.append("Provider should not be empty\n");
				} else if (!(provider).matches("[\\p{L}\\- 0-9]+")) {
					validationResult.append("Provider is not valid\n");
				}
				return validationResult;
			}

		});
		if (inputDialog.open() == Window.OK) {
			CheatSheetCatalogReference catalogReference = catalog.getReference();
			catalog = new CheatSheetCatalog(catalog.getName(), inputDialog.getProvider());
			catalog.setReference(catalogReference);
			catalog.setDescription(inputDialog.getDescription());
			catalog.setReadOnly(false);
			for (CheatSheetCategory child : children) {
				catalog.addChild(child);
			}
			root.addChild(catalog);
			this.cheatSheetCatalogView.refreshViewerKeepFolding();
			ModelPersistence.saveCheatSheetCatalogs();
		}
	}
}