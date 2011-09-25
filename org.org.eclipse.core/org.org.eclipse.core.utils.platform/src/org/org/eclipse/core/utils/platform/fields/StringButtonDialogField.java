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
package org.org.eclipse.core.utils.platform.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.org.eclipse.core.utils.platform.dialogs.IStringButtonAdapter;


/**
 * Dialog field containing a label, text control and a button control.
 */
public class StringButtonDialogField extends StringDialogField {

	private Button fBrowseButton;

	private String fBrowseButtonLabel;

	private IStringButtonAdapter fStringButtonAdapter;

	private boolean fButtonEnabled;

	public StringButtonDialogField(IStringButtonAdapter adapter) {
		fStringButtonAdapter = adapter;
		fBrowseButtonLabel = "!Browse...!"; //$NON-NLS-1$
		fButtonEnabled = true;
	}

	/**
	 * Sets the label of the button.
	 */
	public void setButtonLabel(String label) {
		fBrowseButtonLabel = label;
	}

	// ------ adapter communication

	/**
	 * Programmatical pressing of the button
	 */
	public void changeControlPressed() {
		fStringButtonAdapter.changeControlPressed(this);
	}

	// ------- layout helpers

	/*
	 * @see DialogField#doFillIntoGrid
	 */
	public Control[] doFillIntoTable(Composite parent, int nColumns) {

		Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Control text = getTextLayoutControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 2));
		Button button = getChangeControl(parent);
		button.setLayoutData(gridDataForButton(button, 1));
		return new Control[] { label, text, button };
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */
	public int getNumberOfControls() {
		return 3;
	}

	protected static GridData gridDataForButton(Button button, int span) {
		GridData td = new GridData();
		td.horizontalSpan = span;
		return td;
	}

	// ------- ui creation

	/**
	 * Creates or returns the created buttom widget.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> if the widget has already been created.
	 */
	public Button getChangeControl(Composite parent) {
		if (fBrowseButton == null) {
			assertCompositeNotNull(parent);
			fBrowseButton = new Button(parent, SWT.WRAP);
			fBrowseButton.setText(fBrowseButtonLabel);
			fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
			fBrowseButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					changeControlPressed();
				}

				public void widgetSelected(SelectionEvent e) {
					changeControlPressed();
				}
			});

		}
		return fBrowseButton;
	}

	// ------ enable / disable management

	/**
	 * Sets the enable state of the button.
	 */
	public void enableButton(boolean enable) {
		if (isOkToUse(fBrowseButton)) {
			fBrowseButton.setEnabled(isEnabled() && enable);
		}
		fButtonEnabled = enable;
	}

	/*
	 * @see DialogField#updateEnableState
	 */
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fBrowseButton)) {
			fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
		}
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

}