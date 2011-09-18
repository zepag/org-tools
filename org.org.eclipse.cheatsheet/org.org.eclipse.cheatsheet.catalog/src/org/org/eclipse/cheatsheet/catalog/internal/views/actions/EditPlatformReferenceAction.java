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
import org.org.eclipse.cheatsheet.catalog.internal.dialogs.CheatSheetPlatformReferenceDialog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReferenceType;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.eclipse.cheatsheet.catalog.internal.model.Tags;
import org.org.eclipse.cheatsheet.catalog.internal.views.CheatSheetCatalogView;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.enabling.EnabledForWritablePlatformCheatSheetReferenceAction;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;

public class EditPlatformReferenceAction extends EnabledForWritablePlatformCheatSheetReferenceAction {
	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;

	public EditPlatformReferenceAction(CheatSheetCatalogView cheatSheetCatalogView, CheatSheetCatalogView actionHost) {
		super(actionHost);
		this.cheatSheetCatalogView = cheatSheetCatalogView;
	}

	public void run() {
		IStructuredSelection structuredSelection = (IStructuredSelection) this.cheatSheetCatalogView.getViewer().getSelection();
		CheatSheetReference reference = (CheatSheetReference) structuredSelection.getFirstElement();
		CheatSheetCategory category = reference.getParent();
		CheatSheetPlatformReferenceDialog inputDialog = new CheatSheetPlatformReferenceDialog(this.cheatSheetCatalogView.getViewSite().getShell());
		inputDialog.initEdit(reference);
		inputDialog.setValidator(new CategoryAwareFieldsValidator(reference.getParent()) {

			
			@SuppressWarnings("rawtypes")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				String name = (String) fieldValueHolders.get(CheatSheetPlatformReferenceDialog.NAME_FIELD).getValue();
				if (name.trim().length() == 0) {
					validationResult.append("Name should not be empty\n");
				} else if (!(name).matches("[\\p{L}\\- 0-9]+")) {
					validationResult.append("Name is not valid\n");
				}
				String url = (String) fieldValueHolders.get(CheatSheetPlatformReferenceDialog.URL_FIELD).getValue();
				if (url.trim().length() == 0) {
					validationResult.append("Url should not be empty\n");
				}
				String tags = (String) fieldValueHolders.get(CheatSheetPlatformReferenceDialog.TAGS_FIELD).getValue();
				if (!(tags).matches("[\\p{L},]+")) {
					validationResult.append("Tags list is not valid.\n");
				}
				return validationResult;
			}

		});
		if (inputDialog.open() == Window.OK) {
			reference = new CheatSheetReference(CheatSheetReferenceType.PLATFORM, inputDialog.getId(), inputDialog.getName(), "platform:/" + inputDialog.getUrl(), new Tags(inputDialog.getTags()));
			reference.setDescription(inputDialog.getDescription());
			category.addChild(reference);
			this.cheatSheetCatalogView.refreshViewerKeepFolding();
			ModelPersistence.saveCheatSheetCatalogs();
			this.cheatSheetCatalogView.touchTags();
		}
	}
}