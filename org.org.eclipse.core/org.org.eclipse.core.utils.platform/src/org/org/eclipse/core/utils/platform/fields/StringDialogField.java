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

import java.util.Collection;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog field containing a label and a text control.
 */
public class StringDialogField extends DialogField {

	private String fText;

	private Text fTextControl;

	private ModifyListener fModifyListener;

	private String[] fContentProposals = new String[] {};

	private Composite fTextLayoutControl;

	private SimpleContentProposalProvider fProposalProvider = new SimpleContentProposalProvider(fContentProposals);

	public void setContentProposals(String[] contentProposals) {
		this.fContentProposals = contentProposals;
		doSetContentProposals();
	}

	public void setContentProposals(Collection<String> contentProposals) {
		this.fContentProposals = contentProposals.toArray(new String[] {});
		doSetContentProposals();
	}

	private void doSetContentProposals() {
		if (fProposalProvider != null) {
			fProposalProvider.setProposals(this.fContentProposals);
		}
	}

	public StringDialogField() {
		fText = ""; //$NON-NLS-1$
		fProposalProvider.setFiltering(true);
	}

	// ------- layout helpers

	/*
	 * @see DialogField#doFillIntoGrid
	 */
	public Control[] doFillIntoTable(Composite parent, int nColumns) {
		Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Control text = getTextLayoutControl(parent);
		text.setLayoutData(gridDataForText(nColumns - 1));
		return new Control[] { label, text };
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */
	public int getNumberOfControls() {
		return 2;
	}

	protected static GridData gridDataForText(int span) {
		GridData td = new GridData(SWT.FILL, SWT.CENTER, true, false, span, 1);
		td.horizontalIndent = 5;
		return td;
	}

	// ------- focus methods

	/*
	 * @see DialogField#setFocus
	 */
	public boolean setFocus() {
		if (isOkToUse(fTextControl)) {
			fTextControl.setFocus();
			fTextControl.setSelection(0, fTextControl.getText().length());
		}
		return true;
	}

	// ------- ui creation

	/**
	 * Creates or returns the created text control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has already been created.
	 */
	public Control getTextLayoutControl(Composite parent) {
		if (fTextLayoutControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};
			fTextLayoutControl = new Composite(parent, SWT.NONE);
			fTextLayoutControl.setLayout(new GridLayout(1, true));
			fTextControl = new Text(fTextLayoutControl, SWT.BORDER);
			fTextControl.setText(fText);
			fTextControl.setFont(parent.getFont());
			fTextControl.addModifyListener(fModifyListener);
			fTextControl.setEnabled(isEnabled());
			fTextControl.setLayoutData(new GridData(GridData.FILL_BOTH));

			ContentProposalAdapter adapter = new ContentProposalAdapter(fTextControl, new TextContentAdapter(), fProposalProvider, getContentProposalKeyStroke(), null);
			adapter.setPropagateKeys(true);
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			ControlDecoration controlDecoration = new ControlDecoration(fTextControl, SWT.LEFT | SWT.TOP);
			controlDecoration.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());

		}
		return fTextLayoutControl;
	}

	protected KeyStroke getContentProposalKeyStroke() {
		KeyStroke result = null;
		try {
			result = KeyStroke.getInstance("Ctrl" + KeyStroke.KEY_DELIMITER + "Space");
		} catch (ParseException e) {
			// fail silently
		}
		return result;
	}

	public Text getTextControl(Composite parent) {
		if (fTextControl == null) {
			getTextLayoutControl(parent);
		}
		return fTextControl;
	}

	private void doModifyText(ModifyEvent e) {
		if (isOkToUse(fTextControl)) {
			fText = fTextControl.getText();
		}
		dialogFieldChanged();
	}

	// ------ enable / disable management

	/*
	 * @see DialogField#updateEnableState
	 */
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fTextControl)) {
			fTextControl.setEnabled(isEnabled());
		}
	}

	// ------ text access

	/**
	 * Gets the text. Can not be <code>null</code>
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Sets the text. Triggers a dialog-changed event.
	 */
	public void setText(String text) {
		fText = text;
		if (isOkToUse(fTextControl)) {
			fTextControl.setText(text);
		} else {
			dialogFieldChanged();
		}
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 */
	public void setTextWithoutUpdate(String text) {
		fText = text;
		if (isOkToUse(fTextControl)) {
			fTextControl.removeModifyListener(fModifyListener);
			fTextControl.setText(text);
			fTextControl.addModifyListener(fModifyListener);
		}
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	/**
	 * @see DialogField#refresh()
	 */
	public void refresh() {
		super.refresh();
		if (isOkToUse(fTextControl)) {
			setTextWithoutUpdate(fText);
		}
	}
}