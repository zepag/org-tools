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
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.StringHolder;

@SuppressWarnings("unchecked")
public class CheatSheetCatalogDialog extends AbstractInputTrayDialog {

	public static final IFieldIdentifier NAME_FIELD = new SimpleFieldIdentifier("name");
	public static final IFieldIdentifier PROVIDER_FIELD = new SimpleFieldIdentifier("provider");
	public static final IFieldIdentifier DESCRIPTION_FIELD = new SimpleFieldIdentifier("description");

	private StringHolder nameHolder = new StringHolder(NAME_FIELD);
	private StringHolder providerHolder = new StringHolder(PROVIDER_FIELD);
	private StringHolder descriptionHolder = new StringHolder(DESCRIPTION_FIELD);

	boolean editMode = false;

	public String getName() {
		return (String) getFieldValueHolderValue(NAME_FIELD);
	}

	public String getProvider() {
		return (String) getFieldValueHolderValue(PROVIDER_FIELD);
	}

	public String getDescription() {
		return (String) getFieldValueHolderValue(DESCRIPTION_FIELD);
	}

	public CheatSheetCatalogDialog(Shell shell) {
		super(shell, "Cheat sheet Catalog");
		setValidationMessage("no message");
	}

	public void initEdit(CheatSheetCatalog cheatSheetCatalog) {
		editMode = true;
		nameHolder.setValue(cheatSheetCatalog.getName());
		providerHolder.setValue(cheatSheetCatalog.getProvider());
		setValidationMessage("edit message");
	}

	public void initDuplicate(CheatSheetCatalog cheatSheetCatalog) {
		nameHolder.setValue(cheatSheetCatalog.getName());
		providerHolder.setValue(cheatSheetCatalog.getProvider());
		setValidationMessage("edit message");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createTextField(composite, nameHolder, "Name", !editMode);
		createTextField(composite, providerHolder, "Provider");
		createTextAreaField(composite, descriptionHolder, "Description", 3);
		return composite;
	}

}
