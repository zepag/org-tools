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
package org.org.eclipse.cheatsheet.commands.internal.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.cheatsheet.commands.handlers.ModeParameterValues;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.StringHolder;

public class LocalFileCreationPromptDialog extends AbstractInputTrayDialog {

	public static final IFieldIdentifier CHOSEN_PATH_FIELD = new SimpleFieldIdentifier("CHOSEN_PROJECT_FIELD");
	public static final IFieldIdentifier MODE_PROJECT_FIELD = new SimpleFieldIdentifier("MODE_PROJECT_FIELD");

	private IFieldValueHolder<String> chosenPathHolder = new StringHolder(CHOSEN_PATH_FIELD);
	private IFieldValueHolder<String> modeHolder = new StringHolder(MODE_PROJECT_FIELD);
	private boolean promptForMode;
	private String defaultPath;

	public String getChosenPath() {
		return (String) getFieldValueHolderValue(CHOSEN_PATH_FIELD);
	}

	public String getMode() {
		return (String) getFieldValueHolderValue(MODE_PROJECT_FIELD);
	}

	/**
	 * Creates a resource selection dialog rooted at the given element.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param rootElement
	 *            the root element to populate this dialog with
	 * @param message
	 *            the message to be displayed at the top of this dialog, or <code>null</code> to display a default message
	 */
	public LocalFileCreationPromptDialog(Shell parentShell, String defaultPath, boolean promptForMode) {
		super(parentShell, "Local File creation prompt");
		this.defaultPath = defaultPath;
		this.promptForMode = promptForMode;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		chosenPathHolder.setValue(defaultPath);
		createFolderChoiceField(composite, chosenPathHolder, "Path", true);
		if (promptForMode) {
			String[] modes = new String[] { ModeParameterValues.REPLACE, ModeParameterValues.SKIP, ModeParameterValues.SUFFIX };
			createSingleChoiceComboField(composite, modeHolder, "Write mode", modes);
		}
		return composite;

	}
}
