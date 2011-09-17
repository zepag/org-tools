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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.org.eclipse.cheatsheet.catalog.internal.dialogs.CheatSheetUrlReferenceDialog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReferenceType;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.model.Tags;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForWritableCheatSheetCategoryAction;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;

public class AddHttpReferenceAction extends EnabledForWritableCheatSheetCategoryAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public AddHttpReferenceAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		IStructuredSelection structuredSelection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		CheatSheetCategory category = (CheatSheetCategory) structuredSelection.getFirstElement();
		CheatSheetUrlReferenceDialog inputDialog = new CheatSheetUrlReferenceDialog(this.cheatSheetCatalogView.getViewSite().getShell());
		inputDialog.setValidator(new CategoryAwareFieldsValidator(category) {

			@SuppressWarnings("unchecked")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				String id = (String) fieldValueHolders.get(CheatSheetUrlReferenceDialog.ID_FIELD).getValue();
				if (id.trim().length() == 0) {
					validationResult.append("Id should not be empty\n");
				} else if (!(id).matches("[\\p{L}\\- 0-9]+")) {
					validationResult.append("Id is not valid\n");
				} else if (getCategory().hasChild(id)) {
					validationResult.append("Reference's id must not already exist in the same category\n");
				}
				String name = (String) fieldValueHolders.get(CheatSheetUrlReferenceDialog.NAME_FIELD).getValue();
				if (name.trim().length() == 0) {
					validationResult.append("Name should not be empty\n");
				} else if (!(name).matches("[\\p{L}\\- 0-9]+")) {
					validationResult.append("Name is not valid\n");
				}
				String url = (String) fieldValueHolders.get(CheatSheetUrlReferenceDialog.URL_FIELD).getValue();
				if (url.trim().length() == 0) {
					validationResult.append("Url should not be empty\n");
				} else {
					try {
						URL urlObj = new URL(url);
						Proxy proxy = IOToolBox.determineProxy(urlObj);
						urlObj.openConnection(proxy).getInputStream();
					} catch (MalformedURLException e) {
						validationResult.append("Malformed Url:\n\t" + e.getMessage());
					} catch (IOException e) {
						validationResult.append("Impossible to reach Url:\n\t" + e.getMessage());
					}
				}
				String tags = (String) fieldValueHolders.get(CheatSheetUrlReferenceDialog.TAGS_FIELD).getValue();
				if (!(tags).matches("[\\p{L},]+")) {
					validationResult.append("Tags list is not valid.\n");
				}
				return validationResult;
			}

		});
		if (inputDialog.open() == Window.OK) {
			CheatSheetReference cheatSheetReference = new CheatSheetReference(CheatSheetReferenceType.HTTP, inputDialog.getId(), inputDialog.getName(), inputDialog.getUrl(), new Tags(inputDialog.getTags()));
			cheatSheetReference.setDescription(inputDialog.getDescription());
			category.addChild(cheatSheetReference);
			this.cheatSheetCatalogView.refreshViewerKeepFolding();
			ModelPersistence.saveCheatSheetCatalogs();
			this.cheatSheetCatalogView.touchTags();
		}
	}
}