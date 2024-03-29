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

import java.util.UUID;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCategory;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.StringHolder;

public class CheatSheetCategoryDialog extends AbstractInputTrayDialog {
	public static final IFieldIdentifier NAME_FIELD = new SimpleFieldIdentifier("name");
	public static final IFieldIdentifier UID_FIELD = new SimpleFieldIdentifier("uid");

	private StringHolder nameHolder = new StringHolder(NAME_FIELD);
	private StringHolder uidHolder = new StringHolder(UID_FIELD);

	public String getName() {
		return (String) getFieldValueHolderValue(NAME_FIELD);
	}

	public String getUid() {
		return (String) getFieldValueHolderValue(UID_FIELD);
	}

	public CheatSheetCategoryDialog(Shell shell) {
		super(shell, "Cheat sheet category");
	}

	public void initEdit(CheatSheetCategory cheatSheetCategory) {
		nameHolder.setValue(cheatSheetCategory.getName());
		uidHolder.setValue(cheatSheetCategory.getUID());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		if (uidHolder.getValue().trim().equals("")) {
			uidHolder.setValue(UUID.randomUUID().toString());
		}
		createTextField(composite, uidHolder, "UID", false);
		createTextField(composite, nameHolder, "Name");
		return composite;
	}

}
