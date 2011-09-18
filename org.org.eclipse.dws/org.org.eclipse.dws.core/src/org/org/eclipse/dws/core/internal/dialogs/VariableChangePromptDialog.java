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
 * The Class VariableChangePromptDialog.
 */
public class VariableChangePromptDialog extends AbstractInputTrayDialog {
	
	/** The Constant ORIGINAL_FIELD. */
	public static final IFieldIdentifier ORIGINAL_FIELD = new SimpleFieldIdentifier("ORIGINAL_FIELD");
	
	/** The Constant TARGET_FIELD. */
	public static final IFieldIdentifier TARGET_FIELD = new SimpleFieldIdentifier("TARGET_FIELD");

	/** The property original holder. */
	private IFieldValueHolder<String> propertyOriginalHolder = new StringHolder(ORIGINAL_FIELD);
	
	/** The property target holder. */
	private IFieldValueHolder<String> propertyTargetHolder = new StringHolder(TARGET_FIELD);

	/**
	 * Gets the original variable.
	 * 
	 * @return the original variable
	 */
	public String getOriginalVariable() {
		return (String) getFieldValueHolderValue(ORIGINAL_FIELD);
	}

	/**
	 * Gets the target variable.
	 * 
	 * @return the target variable
	 */
	public String getTargetVariable() {
		return (String) getFieldValueHolderValue(TARGET_FIELD);
	}

	/**
	 * Instantiates a new variable change prompt dialog.
	 * 
	 * @param parentShell the parent shell
	 */
	public VariableChangePromptDialog(Shell parentShell) {
		super(parentShell, "Input variable names");
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
		createTextField(composite, propertyOriginalHolder, "Original");
		createTextField(composite, propertyTargetHolder, "Target");
		return composite;
	}
}