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
package org.org.eclipse.core.utils.platform.dialogs.input;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractInputTrayDialog extends TrayDialog {
	public static enum FieldType {
		TEXT, TEXT_AREA, MULTIPLE_CHOICE_COMBO, SINGLE_CHOICE_COMBO, FILE_CHOICE, FOLDER_CHOICE
	}

	private int numColumns = 2;
	private String title;
	private Text validationMessageLabel;
	private IFieldsValidator validator;
	@SuppressWarnings("rawtypes")
	private Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders = new ConcurrentHashMap<IFieldIdentifier, IFieldValueHolder>();

	public AbstractInputTrayDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	public AbstractInputTrayDialog(Shell shell) {
		super(shell);
	}

	public AbstractInputTrayDialog(Shell shell, int numColumns) {
		super(shell);
		this.numColumns = numColumns;
	}

	public AbstractInputTrayDialog(Shell shell, String title) {
		super(shell);
		this.title = title;
	}

	public AbstractInputTrayDialog(Shell shell, String title, int numColums) {
		super(shell);
		this.title = title;
		this.numColumns = numColums;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(composite);
		if (title != null) {
			Composite titleContainer = new Composite(composite, SWT.NONE);
			titleContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			titleContainer.setBackground(JFaceColors.getBannerBackground(this.getShell().getDisplay()));
			titleContainer.setLayout(new GridLayout(1, false));
			Label nameLabel = new Label(titleContainer, SWT.NONE);
			nameLabel.setText(title);
			nameLabel.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
			nameLabel.setBackground(JFaceColors.getBannerBackground(this.getShell().getDisplay()));
			nameLabel.setForeground(JFaceColors.getBannerForeground(this.getShell().getDisplay()));
			nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		Composite fieldsContainer = new Composite(composite, SWT.BORDER);
		fieldsContainer.setLayout(new GridLayout(numColumns, false));
		fieldsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final Composite validationMessageContainer = new Composite(composite, SWT.BORDER);
		validationMessageContainer.setLayout(new GridLayout(1, false));
		validationMessageContainer.setBackground(JFaceColors.getBannerBackground(this.getShell().getDisplay()));
		validationMessageContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		validationMessageLabel = new Text(validationMessageContainer, SWT.MULTI | SWT.READ_ONLY);
		validationMessageLabel.setText("");
		validationMessageLabel.setForeground(JFaceColors.getErrorText(this.getShell().getDisplay()));
		validationMessageLabel.setBackground(JFaceColors.getBannerBackground(this.getShell().getDisplay()));
		validationMessageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return fieldsContainer;
	}

	@Override
	protected void okPressed() {
		boolean validatorIssue = false;
		if (validator != null) {
			IValidationResult validationResult = validator.validate(fieldValueHolders);
			if (validationResult.getMessage() != null) {
				validatorIssue = true;
				validationMessageLabel.setText(validationResult.getMessage());
				getShell().layout(true);
				getShell().pack(true);
			}
		}
		if (!validatorIssue) {
			super.okPressed();
		}
	}

	protected Text createTextField(Composite composite, IFieldValueHolder<String> valueHolder, final String fieldLabel, boolean enabled) {
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(fieldLabel + ":");
		nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		Text text = new Text(composite, SWT.FLAT | SWT.BORDER);
		text.setText(valueHolder.getValue());
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1));
		text.addModifyListener(new FieldValueModifiedListener(valueHolder));
		text.setEnabled(enabled);
		fieldValueHolders.put(valueHolder.getFieldId(), valueHolder);
		return text;
	}

	protected Text createTextField(Composite composite, IFieldIdentifier fieldIdentifier, final String fieldLabel, boolean enabled) {
		IFieldValueHolder<String> fieldValueHolder = new StringHolder(fieldIdentifier);
		return createTextField(composite, fieldValueHolder, fieldLabel, enabled);
	}

	protected Text createTextField(Composite composite, IFieldValueHolder<String> valueHolder, final String fieldLabel) {
		return createTextField(composite, valueHolder, fieldLabel, true);
	}

	protected Text createTextField(Composite composite, IFieldIdentifier fieldIdentifier, final String fieldLabel) {
		return createTextField(composite, fieldIdentifier, fieldLabel, true);
	}

	protected Text createTextAreaField(Composite composite, IFieldValueHolder<String> valueHolder, final String fieldLabel, boolean enabled, int lines) {
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(fieldLabel + ":");
		nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		Text text = new Text(composite, SWT.FLAT | SWT.BORDER | SWT.MULTI);
		GC gc = new GC(text);
		FontMetrics fm = gc.getFontMetrics();
		gc.dispose();
		int height = lines * fm.getHeight();
		text.setText(valueHolder.getValue());
		GridData gridLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1);
		gridLayoutData.heightHint = height;
		text.setLayoutData(gridLayoutData);
		text.addModifyListener(new FieldValueModifiedListener(valueHolder));
		text.setEnabled(enabled);
		fieldValueHolders.put(valueHolder.getFieldId(), valueHolder);
		return text;
	}

	protected Text createTextAreaField(Composite composite, IFieldIdentifier fieldIdentifier, final String fieldLabel, boolean enabled, int lines) {
		IFieldValueHolder<String> fieldValueHolder = new StringHolder(fieldIdentifier);
		return createTextAreaField(composite, fieldValueHolder, fieldLabel, enabled, lines);
	}

	protected Text createTextAreaField(Composite composite, IFieldValueHolder<String> valueHolder, final String fieldLabel, int lines) {
		return createTextAreaField(composite, valueHolder, fieldLabel, true, lines);
	}

	protected Text createTextAreaField(Composite composite, IFieldIdentifier fieldIdentifier, final String fieldLabel, int lines) {
		return createTextAreaField(composite, fieldIdentifier, fieldLabel, true, lines);
	}

	protected List createMultipleChoiceComboField(Composite composite, IFieldValueHolder<String[]> valueHolder, final String fieldLabel, String[] comboContents) {
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		nameLabel.setText(fieldLabel + ":");
		List list = new List(composite, SWT.MULTI | SWT.BORDER);
		list.setItems(comboContents);
		list.addSelectionListener(new ListValuesSelectionListener(valueHolder));
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1);
		gridData.heightHint = 80;
		list.setLayoutData(gridData);
		fieldValueHolders.put(valueHolder.getFieldId(), valueHolder);
		return list;
	}

	protected Combo createSingleChoiceComboField(Composite composite, IFieldValueHolder<String> valueHolder, final String fieldLabel, String[] comboContents) {
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		nameLabel.setText(fieldLabel + ":");
		Combo combo = new Combo(composite, SWT.SINGLE);
		combo.setItems(comboContents);
		combo.addModifyListener(new FieldValueModifiedListener(valueHolder));
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1));
		fieldValueHolders.put(valueHolder.getFieldId(), valueHolder);
		return combo;
	}

	protected Text createFolderChoiceField(final Composite composite, IFieldValueHolder<String> valueHolder, final String fieldLabel, boolean enabled) {
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		nameLabel.setText(fieldLabel + ":");
		int textFlags = SWT.FLAT | SWT.BORDER;
		if (!enabled) {
			textFlags |= SWT.READ_ONLY;
		}
		final Text text = new Text(composite, textFlags);
		text.addModifyListener(new FieldValueModifiedListener(valueHolder));
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 2, 1));
		text.setText(valueHolder.getValue());
		Button button = new Button(composite, SWT.FLAT);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(composite.getShell());
				String file = directoryDialog.open();
				if (file != null) {
					text.setText(file);
				}
			}

		});
		fieldValueHolders.put(valueHolder.getFieldId(), valueHolder);
		return text;
	}

	protected Combo createSingleChoiceComboField(Composite composite, IFieldValueHolder<String> valueHolder, final String fieldLabel, String[] comboValues, Map<String, String> valuesMapping) {
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		nameLabel.setText(fieldLabel + ":");
		Combo combo = new Combo(composite, SWT.SINGLE);
		combo.setItems(comboValues);
		combo.addModifyListener(new FieldValueModifiedListener(valueHolder, valuesMapping));
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 1, 1));
		fieldValueHolders.put(valueHolder.getFieldId(), valueHolder);
		return combo;
	}

	protected Text createFileChoiceField(Composite composite, IFieldValueHolder<String> valueHolder, String fieldLabel, boolean enabled, String[] filterExtensions) {
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		nameLabel.setText(fieldLabel + ":");
		int textFlags = SWT.FLAT | SWT.BORDER;
		if (!enabled) {
			textFlags |= SWT.READ_ONLY;
		}
		final Text text = new Text(composite, textFlags);
		text.setText(valueHolder.getValue());
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, numColumns - 2, 1));
		text.addModifyListener(new FieldValueModifiedListener(valueHolder));
		Button button = new Button(composite, SWT.FLAT);
		button.setText("Browse...");
		button.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 1, 1));
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		button.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
				FileDialog fileDialog = new FileDialog(getShell());
				fileDialog.setFilterExtensions(new String[] { "*.xml" });
				String file = fileDialog.open();
				if (file != null) {
					text.setText(file);
				}
				getShell().layout(true);
				getShell().pack(true);
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		button.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {

			}

			public void keyPressed(KeyEvent e) {
				if (e.character == ' ' || e.character == SWT.KEYPAD_CR) {
					FileDialog fileDialog = new FileDialog(getShell());
					fileDialog.setFilterExtensions(new String[] { "*.xml" });
					String file = fileDialog.open();
					if (file != null) {
						text.setText(file);
					}
					getShell().layout(true);
					getShell().pack(true);
				}
			}

		});
		fieldValueHolders.put(valueHolder.getFieldId(), valueHolder);
		text.setEnabled(enabled);
		return text;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public String getValidationMessage() {
		return validationMessageLabel.getText();
	}

	public void setValidationMessage(String validationMessage) {
		if (validationMessageLabel != null) {
			validationMessageLabel.setText(validationMessage);
		}
	}

	public IFieldsValidator getValidator() {
		return validator;
	}

	public void setValidator(IFieldsValidator validator) {
		this.validator = validator;
	}

	@SuppressWarnings("rawtypes")
	public IFieldValueHolder getFieldValueHolder(IFieldIdentifier fieldIdentifier) {
		return fieldValueHolders.get(fieldIdentifier);
	}

	public Object getFieldValueHolderValue(IFieldIdentifier fieldIdentifier) {
		if (fieldValueHolders.get(fieldIdentifier) == null) {
			throw new IllegalStateException("Field has not yet been created, open dialog first");
		}
		return fieldValueHolders.get(fieldIdentifier).getValue();
	}
}
