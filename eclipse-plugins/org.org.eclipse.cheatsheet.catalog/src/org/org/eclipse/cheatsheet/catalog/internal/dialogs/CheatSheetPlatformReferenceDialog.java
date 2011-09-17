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
package org.org.eclipse.cheatsheet.catalog.internal.dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetCollectionElement;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.StringHolder;

@SuppressWarnings("unchecked")
public class CheatSheetPlatformReferenceDialog extends AbstractInputTrayDialog {
	public static final IFieldIdentifier ID_FIELD = new SimpleFieldIdentifier("id");
	public static final IFieldIdentifier NAME_FIELD = new SimpleFieldIdentifier("name");
	public static final IFieldIdentifier URL_FIELD = new SimpleFieldIdentifier("url");
	public static final IFieldIdentifier TAGS_FIELD = new SimpleFieldIdentifier("tags");
	public static final IFieldIdentifier DESCRIPTION_FIELD = new SimpleFieldIdentifier("description");

	private StringHolder idHolder = new StringHolder(ID_FIELD);
	private StringHolder nameHolder = new StringHolder(NAME_FIELD);
	private StringHolder urlHolder = new StringHolder(URL_FIELD);
	private StringHolder tagsField = new StringHolder(TAGS_FIELD);
	private StringHolder descriptionHolder = new StringHolder(DESCRIPTION_FIELD);

	private boolean editMode = false;

	public CheatSheetPlatformReferenceDialog(Shell shell) {
		super(shell, "Cheat sheet reference");
	}

	public String getTags() {
		return (String) getFieldValueHolderValue(TAGS_FIELD);
	}

	public String getUrl() {
		return (String) getFieldValueHolderValue(URL_FIELD);
	}

	public String getName() {
		return (String) getFieldValueHolderValue(NAME_FIELD);
	}

	public String getId() {
		return (String) getFieldValueHolderValue(ID_FIELD);
	}

	public String getDescription() {
		return (String) getFieldValueHolderValue(DESCRIPTION_FIELD);
	}

	public void initEdit(CheatSheetReference reference) {
		editMode = true;
		idHolder.setValue(reference.getId());
		nameHolder.setValue(reference.getName());
		urlHolder.setValue(reference.getUrl());
		tagsField.setValue(reference.getTags().getTagsString());
	}

	@SuppressWarnings("restriction")
	// FIXME replace with any non-internal API when available (in 3.4 maybe?)
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		CheatSheetCollectionElement cheatSheets = (CheatSheetCollectionElement) CheatSheetRegistryReader.getInstance().getCheatSheets();
		Map<String, String> IDs = new HashMap<String, String>();
		for (Object element : cheatSheets.getChildren()) {
			for (Object element2 : ((CheatSheetCollectionElement) element).getCheatSheets()) {
				CheatSheetElement cheatSheetElement = (CheatSheetElement) element2;
				IDs.put(cheatSheetElement.getID() + " (" + cheatSheetElement.getLabel(cheatSheetElement) + ")", cheatSheetElement.getID());
			}
		}
		createTextField(composite, idHolder, "Id", !editMode);
		createTextField(composite, nameHolder, "Name");
		createSingleChoiceComboField(composite, urlHolder, "Url", new TreeSet<String>(IDs.keySet()).toArray(new String[0]), IDs);
		createTextField(composite, tagsField, "Tags");
		createTextAreaField(composite, descriptionHolder, "Description", 3);

		return composite;
	}
}
