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

import java.util.Set;

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
public class LibraryPackNamePromptDialog extends AbstractInputTrayDialog {

	/** The Constant LIBRARY_PACK_NAME_FIELD. */
	public static final IFieldIdentifier LIBRARY_PACK_NAME_FIELD = new SimpleFieldIdentifier("LIBRARY_PACK_NAME_FIELD");
	/** The Constant LIBRARY_PACK_DESCRIPTION_FIELD. */
	public static final IFieldIdentifier LIBRARY_PACK_DESCRIPTION_FIELD = new SimpleFieldIdentifier("LIBRARY_PACK_DESCRIPTION_FIELD");

	/** The library pack name holder. */
	private IFieldValueHolder<String> libraryPackNameHolder = new StringHolder(LIBRARY_PACK_NAME_FIELD);
	/** The library pack descriptio holder. */
	private IFieldValueHolder<String> libraryPackDescriptionHolder = new StringHolder(LIBRARY_PACK_DESCRIPTION_FIELD);

	private String[] libraryPackNamesProposals = new String[0];

	/**
	 * Gets the library.
	 * 
	 * @return the library
	 */
	public String getLibraryName() {
		return (String) getFieldValueHolderValue(LIBRARY_PACK_NAME_FIELD);
	}

	public String getDescription() {
		return (String) getFieldValueHolderValue(LIBRARY_PACK_DESCRIPTION_FIELD);
	}
	/**
	 * Instantiates a new library prompt dialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 */
	public LibraryPackNamePromptDialog(Shell parentShell, Set<String> libraryPackNamesProposals) {
		super(parentShell, "Choose existing or input new Library Pack name");
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.libraryPackNamesProposals = libraryPackNamesProposals.toArray(this.libraryPackNamesProposals);
	}

	/**
	 * @see org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		createSingleChoiceComboField(composite, libraryPackNameHolder, "Name", libraryPackNamesProposals);
		createTextAreaField(composite, libraryPackDescriptionHolder, "Description", 3);
		return composite;
	}

}