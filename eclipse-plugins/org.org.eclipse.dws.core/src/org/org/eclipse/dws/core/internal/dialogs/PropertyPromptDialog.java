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
 * The Class PropertyPromptDialog.
 */
public class PropertyPromptDialog extends AbstractInputTrayDialog {
	
	/** The Constant PROPERTY_KEY_FIELD. */
	public static final IFieldIdentifier PROPERTY_KEY_FIELD = new SimpleFieldIdentifier("PROPERTY_KEY_FIELD");
	
	/** The Constant PROPERTY_VALUE_FIELD. */
	public static final IFieldIdentifier PROPERTY_VALUE_FIELD = new SimpleFieldIdentifier("PROPERTY_VALUE_FIELD");

	/** The property key holder. */
	private IFieldValueHolder<String> propertyKeyHolder = new StringHolder(PROPERTY_KEY_FIELD);
	
	/** The property value holder. */
	private IFieldValueHolder<String> propertyValueHolder = new StringHolder(PROPERTY_VALUE_FIELD);

	/**
	 * Gets the property key.
	 * 
	 * @return the property key
	 */
	public String getPropertyKey() {
		return (String) getFieldValueHolderValue(PROPERTY_KEY_FIELD);
	}

	/**
	 * Gets the property value.
	 * 
	 * @return the property value
	 */
	public String getPropertyValue() {
		return (String) getFieldValueHolderValue(PROPERTY_VALUE_FIELD);
	}

	/**
	 * Instantiates a new property prompt dialog.
	 * 
	 * @param parentShell the parent shell
	 */
	public PropertyPromptDialog(Shell parentShell) {
		super(parentShell, "Input property");
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
		createTextField(composite, propertyKeyHolder, "Key");
		createTextField(composite, propertyValueHolder, "Value");
		return composite;
	}
}