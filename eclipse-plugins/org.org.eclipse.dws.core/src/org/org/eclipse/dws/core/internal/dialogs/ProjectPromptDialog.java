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

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
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
 * The Class ProjectPromptDialog.
 */
public class ProjectPromptDialog extends AbstractInputTrayDialog {

	/** The Constant CHOSEN_PROJECT_FIELD. */
	public static final IFieldIdentifier CHOSEN_PROJECT_FIELD = new SimpleFieldIdentifier("CHOSEN_PROJECT_FIELD");

	/** The projects. */
	private List<IJavaProject> projects;

	/** The chosen project holder. */
	private IFieldValueHolder<String> chosenProjectHolder = new StringHolder(CHOSEN_PROJECT_FIELD);

	/**
	 * Gets the chosen project.
	 * 
	 * @return the chosen project
	 */
	public String getChosenProject() {
		return (String) getFieldValueHolderValue(CHOSEN_PROJECT_FIELD);
	}

	/**
	 * Creates a resource selection dialog rooted at the given element.
	 * 
	 * @param parentShell the parent shell
	 * @param projects the projects
	 */
	public ProjectPromptDialog(Shell parentShell, List<IJavaProject> projects) {
		super(parentShell, "Choose a project");
		this.projects = projects;
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
		String[] items = new String[projects.size()];
		int i = 0;
		for (IJavaProject project : projects) {
			items[i++] = project.getProject().getName();
		}
		createSingleChoiceComboField(composite, chosenProjectHolder, "Projects", items);
		return composite;
	}
}
