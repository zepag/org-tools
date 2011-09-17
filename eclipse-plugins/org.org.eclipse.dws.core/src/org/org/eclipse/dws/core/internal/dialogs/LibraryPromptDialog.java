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
package org.org.eclipse.dws.core.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.StringHolder;


/**
 * The Class LibraryPromptDialog.
 */
public class LibraryPromptDialog extends AbstractInputTrayDialog {
	
	/** The Constant LIBRARY_FIELD. */
	public static final IFieldIdentifier LIBRARY_FIELD = new SimpleFieldIdentifier("LIBRARY_FIELD");

	/** The library holder. */
	private IFieldValueHolder<String> libraryHolder = new StringHolder(LIBRARY_FIELD);

	/**
	 * Gets the library.
	 * 
	 * @return the library
	 */
	public String getLibrary() {
		return (String) getFieldValueHolderValue(LIBRARY_FIELD);
	}

	/**
	 * Instantiates a new library prompt dialog.
	 * 
	 * @param parentShell the parent shell
	 */
	public LibraryPromptDialog(Shell parentShell) {
		super(parentShell, "Input library");
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/* (non-Javadoc)
	 * @see org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @see org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		createTextField(composite, libraryHolder, "Library");
		return composite;
	}
}