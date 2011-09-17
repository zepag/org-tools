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
 * The Class ClasspathOrFolderPromptDialog.
 */
public class ClasspathOrFolderPromptDialog extends AbstractInputTrayDialog {

	/** The Constant CHOICE_FIELD. */
	public static final IFieldIdentifier CHOICE_FIELD = new SimpleFieldIdentifier("CHOICE_FIELD");

	/** The Constant ADD_TO_CLASSPATH. */
	public final static String ADD_TO_CLASSPATH = "Add libraries to a project's classpath";

	/** The Constant ADD_TO_FOLDER. */
	public final static String ADD_TO_FOLDER = "Add libraries to a project's folder";

	/** The choice holder. */
	private IFieldValueHolder<String> choiceHolder = new StringHolder(CHOICE_FIELD);

	/**
	 * Gets the choice.
	 * 
	 * @return the choice
	 */
	public String getChoice() {
		return (String) getFieldValueHolderValue(CHOICE_FIELD);
	}

	/**
	 * Creates a resource selection dialog rooted at the given element.
	 * 
	 * @param parentShell
	 *            the parent shell
	 */
	public ClasspathOrFolderPromptDialog(Shell parentShell) {
		super(parentShell, "Choose a project");
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @see org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		createSingleChoiceComboField(composite, choiceHolder, "Target", new String[] { ADD_TO_CLASSPATH, ADD_TO_FOLDER });
		return composite;
	}
}
