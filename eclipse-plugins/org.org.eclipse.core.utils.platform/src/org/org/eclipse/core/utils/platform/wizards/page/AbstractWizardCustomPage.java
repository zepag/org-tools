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
package org.org.eclipse.core.utils.platform.wizards.page;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.org.eclipse.core.utils.platform.dialogs.IListAdapter;
import org.org.eclipse.core.utils.platform.dialogs.IStringButtonAdapter;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.DialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogFieldListener;
import org.org.eclipse.core.utils.platform.fields.ListDialogField;
import org.org.eclipse.core.utils.platform.tools.DialogHelper;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;


/**
 * @author pagregoire
 */
public abstract class AbstractWizardCustomPage extends WizardPage {
	/**
	 * Logger for this class
	 */
	private static Logger logger = Logger.getLogger(AbstractWizardCustomPage.class);

	private IStatus wizardStatus;

	private Composite wizardContainer;

	private AllInOneAdapter wizardAdapter;

	private int columnsNumber = 3;

	protected int getColumnsNumber() {
		return columnsNumber;
	}

	protected void setColumnsNumber(int columnsNumber) {
		this.columnsNumber = columnsNumber;
	}

	/**
	 * @return Returns the wizardAdapter.
	 */
	protected AllInOneAdapter getWizardAdapter() {
		return wizardAdapter;
	}

	/**
	 * @param wizardAdapter
	 *            The wizardAdapter to set.
	 */
	protected void setWizardAdapter(AllInOneAdapter wizardAdapter) {
		this.wizardAdapter = wizardAdapter;
	}

	/**
	 * @return Returns the wizardStatus.
	 */
	protected IStatus getWizardStatus() {
		return wizardStatus;
	}

	/**
	 * @param wizardStatus
	 *            The wizardStatus to set.
	 */
	protected void setWizardStatus(IStatus wizardStatus) {
		this.wizardStatus = wizardStatus;
	}

	/**
	 * Constructor for ConfigXmlPage.
	 * 
	 * @param pageName
	 */
	public AbstractWizardCustomPage(String wizardId, String title, String description, int columnsNumber) {
		super(wizardId);
		setColumnsNumber(columnsNumber);
		setTitle(title);
		setDescription(description);
		logger.debug("Created the wizard :" + this.getClass().getName());
	}

	/**
	 * Constructor for ConfigXmlPage.
	 * 
	 * @param pageName
	 */
	public AbstractWizardCustomPage(String wizardId, String title, String description) {
		this(wizardId, title, description, 3);
		setPageComplete(false);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		wizardContainer = new Composite(parent, SWT.FLAT);
		wizardContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout wizardLayout = new GridLayout();
		wizardLayout.numColumns = getColumnsNumber();
		wizardContainer.setLayout(wizardLayout);
		wizardAdapter = new AllInOneAdapter();
		wizardContainer.setVisible(true);
		describe();
		setControl(wizardContainer);
		wizardContainer.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				init();
			}

		});
		wizardContainer.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				init();
			}

		});
		wizardContainer.forceFocus();
	}

	private boolean alreadyInited = false;

	private synchronized void init() {
		if (!alreadyInited) {
			initialize();
			touch();
			alreadyInited = true;
		}
	}

	protected void updateStatus(IStatus status) {
		wizardStatus = status;
		setPageComplete(!wizardStatus.matches(IStatus.ERROR));
		DialogHelper.applyToStatusLine(this, wizardStatus);
	}

	protected IStatus createStatus(int severity, String message) {
		IStatus status = new StatusInfo(severity, message);
		return status;
	}

	public void dispose() {
		super.dispose();
	}

	abstract protected void describe();

	abstract protected void initialize();

	abstract protected void touch();

	abstract protected IStatus validate();

	abstract protected void handleCustomButtonPressed(IListDialogField field, int buttonIndex);

	abstract protected void handleSelectionChanged(IListDialogField field);

	abstract protected void handleDoubleClicked(IListDialogField field);

	abstract protected void handleChangeControlPressed(IDialogField field);

	abstract protected void handleDialogFieldChanged(IDialogField field);

	protected class AllInOneAdapter implements IListAdapter, IStringButtonAdapter, IDialogFieldListener {
		private final Logger logger = Logger.getLogger(AllInOneAdapter.class);

		//
		// ----------IListAdapter
		//
		public void customButtonPressed(IListDialogField field, int index) {
			if (logger.isDebugEnabled()) {
				logger.debug("customButtonPressed" + field + " " + index);
			}
			handleCustomButtonPressed(field, index);
		}

		/**
		 * @see IListAdapter#selectionChanged(ListDialogField)
		 */
		public void selectionChanged(IListDialogField field) {
			if (logger.isDebugEnabled()) {
				logger.debug("selectionChanged" + field);
			}
			handleSelectionChanged(field);
		}

		/**
		 * @see IListAdapter#doubleClicked(ListDialogField)
		 */
		public void doubleClicked(IListDialogField field) {
			if (logger.isDebugEnabled()) {
				logger.debug("doubleClicked" + field);
			}
			handleDoubleClicked(field);
		}

		//
		// ----------IStringButtonAdapter
		//
		/**
		 * @see IStringButtonAdapter#changeControlPressed(DialogField)
		 */
		public void changeControlPressed(IDialogField field) {
			if (logger.isDebugEnabled()) {
				logger.debug("changeControlPressed" + field);
			}
			handleChangeControlPressed(field);
		}

		//
		// ----------IDialogFieldListener
		//
		/**
		 * @see IDialogFieldListener#dialogFieldChanged(DialogField)
		 */
		public void dialogFieldChanged(IDialogField field) {
			if (logger.isDebugEnabled()) {
				logger.debug("dialogFieldChanged(DialogField) - ");
			}
			handleDialogFieldChanged(field);
		}
	}

	public Composite getWizardContainer() {
		return wizardContainer;
	}

}