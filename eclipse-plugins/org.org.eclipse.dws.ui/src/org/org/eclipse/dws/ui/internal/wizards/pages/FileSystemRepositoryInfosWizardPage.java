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

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogField;
import org.org.eclipse.core.utils.platform.fields.StringDialogField;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.core.utils.platform.wizards.page.WizardContentsHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.eclipse.dws.ui.internal.wizards.NewRepositoryWizard;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.repository.crawler.items.IFileSystemCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.AbstractCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.FileSystemCrawledRepositorySetup;

/**
 * The Class FileSystemRepositoryInfosWizardPage.
 * 
 * @author pagregoire
 */
public class FileSystemRepositoryInfosWizardPage extends AbstractRepositoryInfosWizardPage<IFileSystemCrawledRepositorySetup> {

	/** The name dialog field. */
	private StringDialogField nameDialogField;

	/** The path dialog field. */
	private StringDialogField pathDialogField;

	/**
	 * Instantiates a new file system repository infos wizard page.
	 */
	public FileSystemRepositoryInfosWizardPage() {
		super(WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_id, WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_title, WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_description);
		setColumnsNumber(1);
	}

	/**
	 * Instantiates a new file system repository infos wizard page.
	 * 
	 * @param label
	 *            the label
	 * @param repositorySetup
	 *            the repository setup
	 */
	public FileSystemRepositoryInfosWizardPage(String label, IFileSystemCrawledRepositorySetup repositorySetup) {
		this();
		this.label = label;
		this.repositorySetup = repositorySetup;
	}

	/**
	 * Describe.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#describe()
	 */
	@Override
	protected void describe() {

		Composite repositoryInfocomposite = describeRepositoryInfoSection(getWizardContainer());
		repositoryInfocomposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite filtersComposite = describeGroupFiltersSection(getWizardContainer());
		filtersComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

	}

	/**
	 * Describe repository info section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describeRepositoryInfoSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		nameDialogField = new StringDialogField();
		nameDialogField.setLabelText(WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_repositoryName);
		nameDialogField.setDialogFieldListener(getWizardAdapter());
		nameDialogField.doFillIntoTable(composite, 3);
		nameDialogField.setContentProposals(RepositoryModelPersistence.getRepositoryNameAutocompleteProposals());

		pathDialogField = new StringDialogField();
		pathDialogField.setLabelText(WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_repositoryPath);
		pathDialogField.setDialogFieldListener(getWizardAdapter());
		pathDialogField.doFillIntoTable(composite, 2);
		pathDialogField.setContentProposals(RepositoryModelPersistence.getFSBrowsedRepositoryAutocompleteProposals());

		Button browseButton = new Button(composite, SWT.NONE);
		browseButton.setText(WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_browse);
		browseButton.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
				pathDialogField.setText(doBrowseFolders());
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		return composite;
	}

	/**
	 * Do browse folders.
	 * 
	 * @return the string
	 */
	private String doBrowseFolders() {
		DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
		String result = directoryDialog.open();
		return result == null ? "" : result; //$NON-NLS-1$
	}

	/**
	 * Initialize.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#initialize()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void initialize() {
		nameDialogField.setFocus();
		if(getWizard() instanceof NewRepositoryWizard){
			NewRepositoryWizard newRepositoryWizard=(NewRepositoryWizard)getWizard();
			IFileSystemCrawledRepositorySetup chosenSetup=(IFileSystemCrawledRepositorySetup)newRepositoryWizard.getChosenRepositorySetup();
			if(chosenSetup!=null){
				this.repositorySetup=chosenSetup;
			}
		}
		if (repositorySetup != null) {
			nameDialogField.setText(repositorySetup.getId());
			pathDialogField.setText(repositorySetup.getBasePath());
			if (repositorySetup.getGroupFilters().size() != 0) {
				setGroupFilters(repositorySetup.getGroupFilters());
				getGroupFiltersDialogField().addElements(new ArrayList(getGroupFilters()));
			}
		} else {
			getGroupFilters().add(AbstractCrawledRepositorySetup.KEEP_ALL_PATTERN);
			getGroupFiltersDialogField().addElement(AbstractCrawledRepositorySetup.KEEP_ALL_PATTERN);
		}
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
		IStatus status = new StatusInfo();
		if (nameDialogField == null || nameDialogField.getText().trim().equals("")) { //$NON-NLS-1$
			status = new StatusInfo(IStatus.ERROR, WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_inputName);
		} else if (pathDialogField == null || pathDialogField.getText().trim().equals("")) { //$NON-NLS-1$
			status = new StatusInfo(IStatus.ERROR, WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_inputPath);
		} else {
			status = new StatusInfo(IStatus.INFO, WizardsMessages.FileSystemBrowsedRepositoryInfosWizardPage_info_startbrowsing);
			label = nameDialogField.getText();
			repositorySetup = new FileSystemCrawledRepositorySetup(pathDialogField.getText());
		}
		return status;
	}

	/**
	 * Adds the group filters.
	 * 
	 * @param repositorySetup
	 *            the repository setup
	 * @param filters
	 *            the filters
	 * 
	 * @return the i file system repository setup
	 * 
	 * @see org.org.eclipse.dws.ui.internal.wizards.pages.AbstractRepositoryInfosWizardPage#addGroupFilters(org.org.repository.crawler.items.ICrawledRepositorySetup, java.util.Set)
	 */
	@Override
	protected IFileSystemCrawledRepositorySetup addGroupFilters(IFileSystemCrawledRepositorySetup repositorySetup, Set<String> filters) {
		FileSystemCrawledRepositorySetup fileSystemCrawledRepositorySetup = new FileSystemCrawledRepositorySetup(repositorySetup);
		fileSystemCrawledRepositorySetup.setGroupFilters(filters);
		return (IFileSystemCrawledRepositorySetup) fileSystemCrawledRepositorySetup.getImmutable();
	}

	/**
	 * Handle change control pressed.
	 * 
	 * @param field
	 *            the field
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
	 * @param field
	 *            the field
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
	 * @param field
	 *            the field
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
	 * @param field
	 *            the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleSelectionChanged(org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField)
	 */
	@Override
	protected void handleSelectionChanged(IListDialogField field) {
		touch();
	}

}