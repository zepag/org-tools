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
package org.org.eclipse.dws.ui.internal.wizards.pages;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.org.eclipse.core.utils.platform.dialogs.input.FolderSelectionDialog;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogField;
import org.org.eclipse.core.utils.platform.fields.StringButtonDialogField;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage;
import org.org.eclipse.core.utils.platform.wizards.page.WizardContentsHelper;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;

/**
 * The Class ChooseFolderWizardPage.
 */
public class ChooseFolderWizardPage extends AbstractWizardCustomPage {

	/** The Constant WIZARD_PAGE_ID. */
	public static final String WIZARD_PAGE_ID = ChooseFolderWizardPage.class.getName();

	/** The result. */
	private String result = null;

	/** The target path dialog field. */
	private StringButtonDialogField targetPathDialogField;

	/**
	 * Instantiates a new choose folder wizard page.
	 */
	public ChooseFolderWizardPage() {
		super(WIZARD_PAGE_ID, WizardsMessages.ChooseFolderWizardPage_title, WizardsMessages.ChooseFolderWizardPage_description);
		setColumnsNumber(1);
	}

	/**
	 * Gets the chosen folder.
	 * 
	 * @return the chosen folder
	 */
	public String getChosenFolder() {
		return result;
	}

	/**
	 * Describe.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#describe()
	 */
	@Override
	protected void describe() {
		ExpandBar expandBar = WizardContentsHelper.createExpandBar(getWizardContainer());
		expandBar.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite composite = new Composite(expandBar, SWT.NONE);
		GridLayout layout = new GridLayout(3, true);
		composite.setLayout(layout);
		targetPathDialogField = new StringButtonDialogField(getWizardAdapter());
		targetPathDialogField.setLabelText(WizardsMessages.ChooseFolderWizardPage_target_path);
		targetPathDialogField.setButtonLabel(WizardsMessages.ChooseFolderWizardPage_browse);
		targetPathDialogField.setDialogFieldListener(getWizardAdapter());
		targetPathDialogField.doFillIntoTable(composite, 3);
		WizardContentsHelper.createExpandItem(expandBar, composite, WizardsMessages.ChooseFolderWizardPage_targetPath);

	}

	/**
	 * Initialize.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#initialize()
	 */
	@Override
	protected void initialize() {
		targetPathDialogField.setFocus();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String chosenProject = ((AbstractPomSyncWizardPage) getWizard().getPage(PomJavaSynchronizationWizardPage.WIZARD_PAGE_ID)).getChosenProjectName();
		IProject project = null;
		if (chosenProject != null) {
			project = workspaceRoot.getProject(chosenProject);
		}
		targetPathDialogField.setText(AggregatedProperties.getDefaultLibFolder(project));
	}

	/**
	 * Touch.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#touch()
	 */
	@Override
	protected void touch() {
		updateStatus(validate());
	}

	/**
	 * Validate.
	 * 
	 * @return the i status
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#validate()
	 */
	@Override
	protected IStatus validate() {
		IStatus status = null;
		status = new StatusInfo(IStatus.INFO, WizardsMessages.ChooseFolderWizardPage_info_folder_targetted);
		if (targetPathDialogField.getText().equals("")) { //$NON-NLS-1$
			status = new StatusInfo(IStatus.ERROR, WizardsMessages.ChooseFolderWizardPage_error_folder_not_targetted);
		}
		result = targetPathDialogField.getText();
		return status;
	}

	/**
	 * Handle change control pressed.
	 * 
	 * @param field the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleChangeControlPressed(org.org.eclipse.core.utils.platform.fields.IDialogField)
	 */
	@Override
	protected void handleChangeControlPressed(IDialogField field) {
		IFolder folder = doBrowseFolders();
		if (folder != null) {
			targetPathDialogField.setTextWithoutUpdate(folder.getProjectRelativePath().toString());
		}
		touch();
	}

	/**
	 * Handle custom button pressed.
	 * 
	 * @param field the field
	 * @param buttonIndex the button index
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleCustomButtonPressed(org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField, int)
	 */
	@Override
	protected void handleCustomButtonPressed(IListDialogField field, int buttonIndex) {
		touch();
	}

	/**
	 * Handle dialog field changed.
	 * 
	 * @param field the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleDialogFieldChanged(org.org.eclipse.core.utils.platform.fields.IDialogField)
	 */
	@Override
	protected void handleDialogFieldChanged(IDialogField field) {
		touch();
	}

	/**
	 * Handle double clicked.
	 * 
	 * @param field the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleDoubleClicked(org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField)
	 */
	@Override
	protected void handleDoubleClicked(IListDialogField field) {
		touch();
	}

	/**
	 * Handle selection changed.
	 * 
	 * @param field the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleSelectionChanged(org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField)
	 */
	@Override
	protected void handleSelectionChanged(IListDialogField field) {
		touch();
	}

	/**
	 * Do browse folders.
	 * 
	 * @return the i folder
	 */
	private IFolder doBrowseFolders() {
		String projectName = ((AbstractPomSyncWizardPage) getWizard().getPage(PomJavaSynchronizationWizardPage.WIZARD_PAGE_ID)).getChosenProjectName();
		IProject project = PluginToolBox.getCurrentWorkspace().getRoot().getProject(projectName);
		IFolder folder = null;
		ILabelProvider lp = new WorkbenchLabelProvider();
		ITreeContentProvider cp = new WorkbenchContentProvider();
		FolderSelectionDialog dialog = new FolderSelectionDialog(getShell(), lp, cp, WizardsMessages.ChooseFolderWizardPage_choose_folder);
		dialog.setTitle(WizardsMessages.ChooseFolderWizardPage_create_new_folder);
		dialog.setInput(project);
		dialog.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				boolean result = false;
				if (((IResource) element).getType() == IResource.FOLDER) {
					result = true;
				}
				return result;
			}

		});
		if (dialog.open() == Window.OK) {
			folder = (IFolder) dialog.getResult()[0];
		}
		return folder;
	}
}