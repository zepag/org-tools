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
 * The Class PreciseGroupPromptDialog.
 */
public class PreciseGroupPromptDialog extends AbstractInputTrayDialog {
	
	/** The Constant GROUP_FIELD. */
	public static final IFieldIdentifier GROUP_FIELD = new SimpleFieldIdentifier("GROUP_FIELD");

	/** The group holder. */
	private IFieldValueHolder<String> groupHolder = new StringHolder(GROUP_FIELD);

	/**
	 * Gets the precise group.
	 * 
	 * @return the precise group
	 */
	public String getPreciseGroup() {
		return (String) getFieldValueHolderValue(GROUP_FIELD);
	}

	/**
	 * Instantiates a new precise group prompt dialog.
	 * 
	 * @param parentShell the parent shell
	 */
	public PreciseGroupPromptDialog(Shell parentShell) {
		super(parentShell, "Input a groupId");
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
		createTextField(composite, groupHolder, "Group Id");
		return composite;
	}
}
