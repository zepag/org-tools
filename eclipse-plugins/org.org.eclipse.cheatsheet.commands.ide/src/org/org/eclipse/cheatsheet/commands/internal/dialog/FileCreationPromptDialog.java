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

import java.util.List;

import org.eclipse.core.resources.IProject;
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

public class FileCreationPromptDialog extends AbstractInputTrayDialog {

	public static final IFieldIdentifier CHOSEN_PROJECT_FIELD = new SimpleFieldIdentifier("CHOSEN_PROJECT_FIELD");
	public static final IFieldIdentifier MODE_PROJECT_FIELD = new SimpleFieldIdentifier("MODE_PROJECT_FIELD");

	private List<IProject> projects;

	private IFieldValueHolder<String> chosenProjectHolder = new StringHolder(CHOSEN_PROJECT_FIELD);
	private IFieldValueHolder<String> modeHolder = new StringHolder(MODE_PROJECT_FIELD);
	private boolean promptForMode;

	public String getChosenProject() {
		return (String) getFieldValueHolderValue(CHOSEN_PROJECT_FIELD);
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
	public FileCreationPromptDialog(Shell parentShell, List<IProject> projects, boolean promptForMode) {
		super(parentShell, "File creation prompt");
		this.projects = projects;
		this.promptForMode = promptForMode;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		String[] items = new String[projects.size()];
		int i = 0;
		for (IProject project : projects) {
			items[i++] = project.getName();
		}
		createSingleChoiceComboField(composite, chosenProjectHolder, "Projects", items);
		if (promptForMode) {
			String[] modes = new String[] { ModeParameterValues.REPLACE, ModeParameterValues.SKIP, ModeParameterValues.SUFFIX };
			createSingleChoiceComboField(composite, modeHolder, "Write mode", modes);
		}
		return composite;

	}
}
