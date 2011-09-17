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
public class CheatSheetCatalogNameDialog extends AbstractInputTrayDialog {
	public static final IFieldIdentifier NAME_FIELD = new SimpleFieldIdentifier("name");

	private StringHolder nameHolder = new StringHolder(NAME_FIELD);

	public String getName() {
		return (String) getFieldValueHolderValue(NAME_FIELD);
	}

	public CheatSheetCatalogNameDialog(Shell shell) {
		super(shell, "New name for Cheat sheet catalog.");
		setValidationMessage("no message");
	}

	public void initEdit(String name) {
		nameHolder.setValue(name);
		setValidationMessage("edit message");
	}

	public void initDuplicate(CheatSheetCatalog cheatSheetCatalog) {
		nameHolder.setValue(cheatSheetCatalog.getName());
		setValidationMessage("edit message");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createTextField(composite, nameHolder, "Name");
		return composite;
	}

}
