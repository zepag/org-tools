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

import org.eclipse.jface.window.Window;
import org.org.eclipse.cheatsheet.catalog.internal.dialogs.CheatSheetCatalogDialog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelConstants;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.AlwaysEnabledAction;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.model.RootModelItem;

public class AddCatalogAction extends AlwaysEnabledAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public AddCatalogAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		RootModelItem<CheatSheetCatalog> root = RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID);
		CheatSheetCatalogDialog inputDialog = new CheatSheetCatalogDialog(this.cheatSheetCatalogView.getViewSite().getShell());
		inputDialog.setValidator(new CatalogsAwareFieldsValidator(root) {

			
			@SuppressWarnings("rawtypes")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				String name = (String) fieldValueHolders.get(CheatSheetCatalogDialog.NAME_FIELD).getValue();
				if (name.trim().length() == 0) {
					validationResult.append("Name should not be empty\n");
				} else if (!(name).matches("[\\p{L}\\- 0-9]+")) {
					validationResult.append("Name is not valid\n");
				} else if (getRoot().hasChild(name)) {
					validationResult.append("Catalog's name must not already exist in the same category.\n");
				}
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
			CheatSheetCatalog cheatSheetCatalog = new CheatSheetCatalog(inputDialog.getName(), inputDialog.getProvider());
			cheatSheetCatalog.setDescription(inputDialog.getDescription());
			cheatSheetCatalog.setReadOnly(false);
			root.addChild(cheatSheetCatalog);
			this.cheatSheetCatalogView.refreshViewerKeepFolding();
			ModelPersistence.saveCheatSheetCatalogs();
		}
	}
}