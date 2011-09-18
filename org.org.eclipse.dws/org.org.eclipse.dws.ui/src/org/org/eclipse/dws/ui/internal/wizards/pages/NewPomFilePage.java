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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormText;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.ComboDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogField;
import org.org.eclipse.core.utils.platform.fields.StringDialogField;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage;
import org.org.eclipse.core.utils.platform.wizards.page.WizardContentsHelper;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;

/**
 * This wizard page allows to create a new Pom File.
 */
public class NewPomFilePage extends AbstractWizardCustomPage {

	/** The Constant WIZARD_PAGE_ID. */
	public static final String WIZARD_PAGE_ID = NewPomFilePage.class.getName();

	/** The PROJEC t_ names. */
	private final String[] PROJECT_NAMES;

	/** The group id dialog field. */
	private StringDialogField groupIdDialogField;

	/** The artifact id dialog field. */
	private StringDialogField artifactIdDialogField;

	/** The version dialog field. */
	private StringDialogField versionDialogField;

	/** The packaging dialog field. */
	private ComboDialogField packagingDialogField;

	/** The pom contents. */
	private FormText pomContents;

	/** The REPOSITOR y_ group s_ names. */
	private final String[] REPOSITORY_GROUPS_NAMES;

	/** The REPOSITOR y_ artifact s_ names. */
	private final String[] REPOSITORY_ARTIFACTS_NAMES;

	/** The PROJECT. */
	private final IProject PROJECT;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param chosenProject the chosen project
	 * @param projectNames the project names
	 * @param repositoryGroupsNames the repository groups names
	 * @param repositoryArtifactsNames the repository artifacts names
	 */
	public NewPomFilePage(IProject chosenProject, String[] projectNames, String[] repositoryGroupsNames, String[] repositoryArtifactsNames) {
		super(WIZARD_PAGE_ID, WizardsMessages.NewPomFilePage_title, WizardsMessages.NewPomFilePage_description);
		this.PROJECT_NAMES = projectNames;
		this.REPOSITORY_GROUPS_NAMES = repositoryGroupsNames;
		this.REPOSITORY_ARTIFACTS_NAMES = repositoryArtifactsNames;
		this.PROJECT = chosenProject;
		setColumnsNumber(1);
	}

	/**
	 * Gets the group id completions.
	 * 
	 * @return the group id completions
	 */
	private Set<String> getGroupIdCompletions() {
		Set<String> result = new LinkedHashSet<String>();
		result.addAll(getProjectNames());
		for (String groupId : RepositoryModelPersistence.getGroupIdAutocompleteProposals()) {
			result.add(groupId);
		}
		for (String groupId : REPOSITORY_GROUPS_NAMES) {
			result.add(groupId);
		}
		return result;
	}

	/**
	 * Gets the artifact id completions.
	 * 
	 * @return the artifact id completions
	 */
	private Set<String> getArtifactIdCompletions() {
		Set<String> result = new LinkedHashSet<String>();
		result.addAll(getProjectNames());
		for (String artifactId : RepositoryModelPersistence.getArtifactIdAutocompleteProposals()) {
			result.add(artifactId);
		}
		for (String artifactId : REPOSITORY_ARTIFACTS_NAMES) {
			result.add(artifactId);
		}
		return result;
	}

	/**
	 * Gets the project names.
	 * 
	 * @return the project names
	 */
	private Set<String> getProjectNames() {
		Set<String> result = new HashSet<String>();
		for (String projectName : PROJECT_NAMES) {
			result.add(projectName);
		}
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

		Composite targetProjectComposite = describeTargetProjectSection(expandBar);
		targetProjectComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		WizardContentsHelper.createExpandItem(expandBar, targetProjectComposite, WizardsMessages.NewPomFilePage_targetProject);

		Composite projectInfoComposite = describeProjectInfoSection(expandBar);
		projectInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		WizardContentsHelper.createExpandItem(expandBar, projectInfoComposite, WizardsMessages.NewPomFilePage_projectInfo);
	}

	/**
	 * Describe project info section.
	 * 
	 * @param parent the parent
	 * 
	 * @return the composite
	 */
	private Composite describeProjectInfoSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		groupIdDialogField = new StringDialogField();
		groupIdDialogField.setLabelText(WizardsMessages.NewPomFilePage_groupId);
		groupIdDialogField.setDialogFieldListener(getWizardAdapter());
		groupIdDialogField.doFillIntoTable(composite, 2);
		groupIdDialogField.setContentProposals(getGroupIdCompletions());
		artifactIdDialogField = new StringDialogField();
		artifactIdDialogField.setLabelText(WizardsMessages.NewPomFilePage_artifactId);
		artifactIdDialogField.setDialogFieldListener(getWizardAdapter());
		artifactIdDialogField.doFillIntoTable(composite, 2);
		artifactIdDialogField.setContentProposals(getArtifactIdCompletions());
		versionDialogField = new StringDialogField();
		versionDialogField.setLabelText(WizardsMessages.NewPomFilePage_version);
		versionDialogField.setDialogFieldListener(getWizardAdapter());
		versionDialogField.doFillIntoTable(composite, 2);
		packagingDialogField = new ComboDialogField(SWT.SIMPLE | SWT.READ_ONLY);
		packagingDialogField.setLabelText(WizardsMessages.NewPomFilePage_packaging);
		packagingDialogField.setItems(new String[] { "", "pom", "jar", "maven-plugin", "ejb", "war", "ear", "rar", "par" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
		packagingDialogField.setDialogFieldListener(getWizardAdapter());
		packagingDialogField.doFillIntoTable(composite, 2);
		pomContents = new FormText(composite, SWT.MULTI | SWT.FILL);
		pomContents.setWhitespaceNormalized(false);
		pomContents.setText(PomInteractionHelper.formatPomContents(PomInteractionHelper.getPomContents(groupIdDialogField.getText(), artifactIdDialogField.getText(), versionDialogField.getText(), packagingDialogField.getText())), false, false);
		pomContents.setEnabled(false);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		pomContents.setLayoutData(gridData);
		return composite;
	}

	/**
	 * Describe target project section.
	 * 
	 * @param parent the parent
	 * 
	 * @return the composite
	 */
	private Composite describeTargetProjectSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(2, true);
		composite.setLayout(layout);
		Label label = new Label(composite, SWT.FLAT);
		label.setText(PROJECT.getName());
		label.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		return composite;
	}

	/**
	 * Initialize.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#initialize()
	 */
	@Override
	protected void initialize() {
		groupIdDialogField.setFocus();
		versionDialogField.setText("1.0.0-SNAPSHOT"); //$NON-NLS-1$
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
		status = new StatusInfo(IStatus.OK, WizardsMessages.NewPomFilePage_info_project_info_chosen);
		status = validateFieldsFormat(status);
		pomContents.setText(PomInteractionHelper.formatPomContents(PomInteractionHelper.getPomContents(groupIdDialogField.getText(), artifactIdDialogField.getText(), versionDialogField.getText(), packagingDialogField.getText())), false, false);
		return status;
	}

	/**
	 * Validate fields format.
	 * 
	 * @param status the status
	 * 
	 * @return the i status
	 */
	private IStatus validateFieldsFormat(IStatus status) {
		IStatus result = status;
		if (groupIdDialogField.getText().equals("")) { //$NON-NLS-1$
			result = new StatusInfo(IStatus.ERROR, groupIdDialogField.getLabelControl(null).getText() + WizardsMessages.NewPomFilePage_not_empty);
		} else if (!Pattern.matches("[A-Za-z0-9\\.\\-]+", groupIdDialogField.getText())) { //$NON-NLS-1$
			result = new StatusInfo(IStatus.ERROR, groupIdDialogField.getLabelControl(null).getText() + WizardsMessages.NewPomFilePage_alphaNumOnly);
		} else if (artifactIdDialogField.getText().equals("")) { //$NON-NLS-1$
			result = new StatusInfo(IStatus.ERROR, artifactIdDialogField.getLabelControl(null).getText() + WizardsMessages.NewPomFilePage_notEmpty);
		} else if (artifactIdDialogField.getText().contains(" ")) { //$NON-NLS-1$
			result = new StatusInfo(IStatus.ERROR, artifactIdDialogField.getLabelControl(null).getText() + WizardsMessages.NewPomFilePage_noSpaces);
		} else if (versionDialogField.getText().equals("")) { //$NON-NLS-1$
			result = new StatusInfo(IStatus.ERROR, versionDialogField.getLabelControl(null).getText() + WizardsMessages.NewPomFilePage_notEmpty);
		} else if (!Pattern.matches("[0-9]+.[0-9]+.[0-9]+(-[A-Za-z0-9]*)*", versionDialogField.getText())) { //$NON-NLS-1$
			result = new StatusInfo(IStatus.ERROR, versionDialogField.getLabelControl(null).getText() + WizardsMessages.NewPomFilePage_snapshotFormat);
		} else if (packagingDialogField.getText().equals("")) { //$NON-NLS-1$
			result = new StatusInfo(IStatus.ERROR, packagingDialogField.getLabelControl(null).getText() + WizardsMessages.NewPomFilePage_notEmpty);
		} else if (!Pattern.matches("pom|jar|maven-plugin|ejb|war|ear|rar|par", packagingDialogField.getText())) { //$NON-NLS-1$
			result = new StatusInfo(IStatus.ERROR, packagingDialogField.getLabelControl(null).getText() + WizardsMessages.NewPomFilePage_shouldBeChosen);
		}
		return result;
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
	 * Handle change control pressed.
	 * 
	 * @param field the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleChangeControlPressed(org.org.eclipse.core.utils.platform.fields.IDialogField)
	 */
	@Override
	protected void handleChangeControlPressed(IDialogField field) {
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
	 * Gets the chosen project.
	 * 
	 * @return the chosen project
	 */
	public String getChosenProject() {
		return PROJECT.getName();
	}

	/**
	 * Gets the artifact id.
	 * 
	 * @return the artifact id
	 */
	public String getArtifactId() {
		return artifactIdDialogField.getText();
	}

	/**
	 * Gets the group id.
	 * 
	 * @return the group id
	 */
	public String getGroupId() {
		return groupIdDialogField.getText();
	}

	/**
	 * Gets the packaging.
	 * 
	 * @return the packaging
	 */
	public String getPackaging() {
		return packagingDialogField.getText();
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return versionDialogField.getText();
	}
}