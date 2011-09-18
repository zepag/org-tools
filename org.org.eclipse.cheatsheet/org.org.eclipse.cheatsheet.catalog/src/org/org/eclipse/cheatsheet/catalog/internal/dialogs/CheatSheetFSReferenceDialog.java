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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.StringHolder;

public class CheatSheetFSReferenceDialog extends AbstractInputTrayDialog {
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

	public CheatSheetFSReferenceDialog(Shell shell) {
		super(shell, "Cheat sheet reference", 3);
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

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createTextField(composite, idHolder, "Id", !editMode);
		createTextField(composite, nameHolder, "Name");
		createFileChoiceField(composite, urlHolder, "Url", false, new String[] { "*.xml" });
		createTextField(composite, tagsField, "Tags");
		createTextAreaField(composite, descriptionHolder, "Description", 3);
		return composite;
	}

}
