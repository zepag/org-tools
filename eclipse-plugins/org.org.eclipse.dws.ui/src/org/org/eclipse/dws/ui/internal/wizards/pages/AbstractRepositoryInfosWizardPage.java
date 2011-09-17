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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.ListDialogField;
import org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage;
import org.org.eclipse.core.utils.platform.wizards.page.WizardContentsHelper;
import org.org.eclipse.dws.core.internal.dialogs.PreciseGroupPromptDialog;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.repository.crawler.items.ICrawledRepositorySetup;

/**
 * The Class AbstractRepositoryInfosWizardPage.
 */
public abstract class AbstractRepositoryInfosWizardPage<T extends ICrawledRepositorySetup> extends AbstractWizardCustomPage {

	/** The Constant INTERFACE_CLEAR_BUTTON. */
	protected static final int INTERFACE_CLEAR_BUTTON = 2;

	/** The Constant INTERFACE_REMOVE_BUTTON. */
	protected static final int INTERFACE_REMOVE_BUTTON = 1;

	/** The Constant INTERFACE_ADD_BUTTON. */
	protected static final int INTERFACE_ADD_BUTTON = 0;

	/** The group filters. */
	private Set<String> groupFilters;

	/** The group filters dialog field. */
	private ListDialogField groupFiltersDialogField;

	/**
	 * The Class GroupFiltersListLabelProvider.
	 */
	public static class GroupFiltersListLabelProvider extends LabelProvider {
	}

	/** The label. */
	protected String label = null;

	/** The repository setup. */
	protected T repositorySetup = null;

	/**
	 * Instantiates a new abstract repository infos wizard page.
	 * 
	 * @param wizardId
	 *            the wizard id
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 * @param columnsNumber
	 *            the columns number
	 */
	public AbstractRepositoryInfosWizardPage(String wizardId, String title, String description, int columnsNumber) {
		super(wizardId, title, description, columnsNumber);
		groupFilters = new HashSet<String>();
	}

	/**
	 * Instantiates a new abstract repository infos wizard page.
	 * 
	 * @param wizardId
	 *            the wizard id
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 */
	public AbstractRepositoryInfosWizardPage(String wizardId, String title, String description) {
		super(wizardId, title, description);
		groupFilters = new HashSet<String>();
	}

	/**
	 * Gets the repository setup.
	 * 
	 * @return the repository setup
	 */
	public T getRepositorySetup() {
		return finalize(this.repositorySetup, groupFilters);
	}

	/**
	 * Finalize.
	 * 
	 * @param repositorySetup
	 *            the repository setup
	 * @param groupFilters
	 *            the group filters
	 * 
	 * @return the t
	 */
	private T finalize(T repositorySetup, Set<String> groupFilters) {
		return addGroupFilters(repositorySetup, groupFilters);
	}

	/**
	 * Adds the group filters.
	 * 
	 * @param repositorySetup
	 *            the repository setup
	 * @param filters
	 *            the filters
	 * 
	 * @return the t
	 */
	protected abstract T addGroupFilters(T repositorySetup, Set<String> filters);

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Handle custom button pressed.
	 * 
	 * @param field
	 *            the field
	 * @param buttonIndex
	 *            the button index
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleCustomButtonPressed(org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField, int)
	 */
	@Override
	protected void handleCustomButtonPressed(IListDialogField field, int buttonIndex) {
		if (isGroupFiltersButton(field)) {
			switch (buttonIndex) {
			case INTERFACE_ADD_BUTTON:
				String filter = doAddGroupFilter();
				if (filter != null) {
					getGroupFilters().add(filter);
					getGroupFiltersDialogField().addElement(filter);
				}
				break;
			case INTERFACE_REMOVE_BUTTON:
				List<?> selectedElements = getGroupFiltersDialogField().getSelectedElements();
				getGroupFilters().removeAll(selectedElements);
				getGroupFiltersDialogField().removeElements(selectedElements);
				break;
			case INTERFACE_CLEAR_BUTTON:
				getGroupFilters().clear();
				getGroupFiltersDialogField().removeAllElements();
				break;
			default:
				break;
			}
		}
		touch();
	}

	/**
	 * Checks if is group filters button.
	 * 
	 * @param field
	 *            the field
	 * 
	 * @return true, if is group filters button
	 */
	private boolean isGroupFiltersButton(IListDialogField field) {
		return field.equals(getGroupFiltersDialogField());
	}

	/**
	 * Do add group filter.
	 * 
	 * @return the string
	 */
	private String doAddGroupFilter() {
		PreciseGroupPromptDialog preciseGroupPromptDialog = new PreciseGroupPromptDialog(getShell());
		preciseGroupPromptDialog.setValidator(new IFieldsValidator() {
			@SuppressWarnings("unchecked")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				IFieldValueHolder<String> fieldValueHolder = fieldValueHolders.get(PreciseGroupPromptDialog.GROUP_FIELD);
				String value = fieldValueHolder.getValue();
				if (value.trim().equals("")) {
					validationResult.append("Group Id cannot be empty");
				} else if (!value.matches("[A-Za-z0-9-\\\\.\\\\*]*")) {
					validationResult.append("Group Id is invalid");
				}
				return validationResult;
			}

		});
		if (preciseGroupPromptDialog.open() == Window.CANCEL) {
			return null;
		} else {
			return preciseGroupPromptDialog.getPreciseGroup();
		}
	}

	/**
	 * Describe group filters section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	protected Composite describeGroupFiltersSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(3, true);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		// Label title = new Label(composite, SWT.FLAT);
		// title.setLayoutData(data);
		// title.setText(WizardsMessages.AbstractRepositoryInfosWizardPage_info_keepEmptyForFullScan);
		String[] buttonNames = new String[3];
		buttonNames[INTERFACE_ADD_BUTTON] = WizardsMessages.AbstractRepositoryInfosWizardPage_add;
		buttonNames[INTERFACE_REMOVE_BUTTON] = WizardsMessages.AbstractRepositoryInfosWizardPage_remove;
		buttonNames[INTERFACE_CLEAR_BUTTON] = WizardsMessages.AbstractRepositoryInfosWizardPage_clear;
		groupFiltersDialogField = new ListDialogField(getWizardAdapter(), buttonNames, new GroupFiltersListLabelProvider());
		groupFiltersDialogField.setLabelText(WizardsMessages.AbstractRepositoryInfosWizardPage_description);
		groupFiltersDialogField.doFillIntoTable(composite, 3);
		return composite;
	}

	/**
	 * Gets the group filters.
	 * 
	 * @return the group filters
	 */
	protected Set<String> getGroupFilters() {
		return groupFilters;
	}

	/**
	 * Sets the group filters.
	 * 
	 * @param filters
	 *            the new group filters
	 */
	protected void setGroupFilters(Set<String> filters) {
		this.groupFilters = filters;
	}

	/**
	 * Gets the group filters dialog field.
	 * 
	 * @return the group filters dialog field
	 */
	protected ListDialogField getGroupFiltersDialogField() {
		return groupFiltersDialogField;
	}

	/**
	 * Sets the group filters dialog field.
	 * 
	 * @param filtersDialogField
	 *            the new group filters dialog field
	 */
	protected void setGroupFiltersDialogField(ListDialogField filtersDialogField) {
		this.groupFiltersDialogField = filtersDialogField;
	}

	public void refresh() {
		initialize();
	}
}
