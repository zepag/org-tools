package org.org.eclipse.dws.ui.internal.wizards.pages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.ComboDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogField;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage;
import org.org.eclipse.dws.core.DWSCorePlugin;

public class RepositoryChoiceWizardPage extends AbstractWizardCustomPage {

	private ComboDialogField repositoriesCombo;
	private final RepositoryWizardType repositoryWizardType;

	private String chosenDefinitionLabel = "";

	public RepositoryChoiceWizardPage(String wizardId, String title, String description, RepositoryWizardType repositoryWizardType) {
		super(wizardId, title, description);
		this.repositoryWizardType = repositoryWizardType;
	}

	public RepositoryChoiceWizardPage(String wizardId, String title, String description, int columnsNumber, RepositoryWizardType repositoryWizardType) {
		super(wizardId, title, description, columnsNumber);
		this.repositoryWizardType = repositoryWizardType;
	}

	@Override
	protected void describe() {
		Group group = new Group(getWizardContainer(), SWT.NONE);
		group.setLayout(new GridLayout(2, true));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText("Choose from possible definitions");
		repositoriesCombo = new ComboDialogField(SWT.READ_ONLY | SWT.DROP_DOWN | SWT.SIMPLE);
		repositoriesCombo.setLabelText("Available definitions: ");
		repositoriesCombo.setDialogFieldListener(getWizardAdapter());
		repositoriesCombo.doFillIntoTable(group, 2);

	}

	@Override
	protected void handleChangeControlPressed(IDialogField field) {
		touch();
	}

	@Override
	protected void handleCustomButtonPressed(IListDialogField field, int buttonIndex) {
		touch();
	}

	@Override
	protected void handleDialogFieldChanged(IDialogField field) {
		if(field.equals(repositoriesCombo)){
			chosenDefinitionLabel = ((ComboDialogField)field).getText();
		}
		touch();
	}

	@Override
	protected void handleDoubleClicked(IListDialogField field) {
		touch();
	}

	@Override
	protected void handleSelectionChanged(IListDialogField field) {
		touch();
	}

	@Override
	protected void initialize() {
		if (repositoryWizardType == RepositoryWizardType.HTTP) {
			repositoriesCombo.setItems(DWSCorePlugin.getDefault().getHttpRepositoryExtensionsLabels().toArray(new String[] {}));
		}
		if (repositoryWizardType == RepositoryWizardType.FS) {
			repositoriesCombo.setItems(DWSCorePlugin.getDefault().getFileSystemRepositoryExtensionsLabels().toArray(new String[] {}));
		}
	}

	@Override
	protected void touch() {
		updateStatus(validate());
	}

	@Override
	protected IStatus validate() {
		IStatus status = new StatusInfo(Status.OK, "Definition will be based on current configuration");
		if (repositoriesCombo.getSelectionIndex() < 0) {
			status = new StatusInfo(Status.WARNING, "Skipping this page will force you to define the repository definition manually");
		}
		return status;
	}

	public String getChosenRepositoryLabel() {
		return chosenDefinitionLabel;
	}

}