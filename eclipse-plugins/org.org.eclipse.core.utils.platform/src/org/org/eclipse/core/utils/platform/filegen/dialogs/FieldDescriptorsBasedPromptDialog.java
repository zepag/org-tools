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
package org.org.eclipse.core.utils.platform.filegen.dialogs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.StringHolder;

public class FieldDescriptorsBasedPromptDialog extends AbstractInputTrayDialog {

	public static class FieldDescriptor {
		private final String fieldLabel;
		private final String fieldId;
		private final String defaultValue;
		private final Boolean mandatory;
		private final FieldType fieldType;
		private final Boolean readOnly;
		private String[] possibleValues;

		public FieldDescriptor(String fieldId, String fieldLabel, String defaultValue) {
			this(fieldId, fieldLabel, defaultValue, false, FieldType.TEXT);
		}

		public FieldDescriptor(String fieldId, String fieldLabel, String defaultValue, Boolean mandatory, FieldType fieldType) {
			super();
			this.fieldLabel = fieldLabel;
			this.fieldId = fieldId;
			this.defaultValue = defaultValue;
			this.mandatory = mandatory;
			this.fieldType = fieldType;
			this.readOnly = false;
		}

		public FieldDescriptor(String fieldId, String fieldLabel, String defaultValue, Boolean mandatory, FieldType fieldType, Boolean readOnly) {
			super();
			this.fieldLabel = fieldLabel;
			this.fieldId = fieldId;
			this.defaultValue = defaultValue;
			this.mandatory = mandatory;
			this.fieldType = fieldType;
			this.readOnly = readOnly;
		}

		public String getFieldLabel() {
			return fieldLabel;
		}

		public String getFieldId() {
			return fieldId;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public Boolean getMandatory() {
			return mandatory;
		}

		public FieldType getFieldType() {
			return fieldType;
		}

		public String[] getPossibleValues() {
			return possibleValues;
		}

		public void setPossibleValues(String[] possibleValues) {
			this.possibleValues = possibleValues;
		}

		public Boolean getReadOnly() {
			return readOnly;
		}
		@Override
		public String toString() {
			return fieldId+":"+fieldLabel+":"+fieldType+":"+(mandatory?"MANDATORY":"OPTIONAL")+":default="+defaultValue+":possibleValues="+possibleValues;
		}

	}

	private final List<FieldDescriptor> fieldDescriptors;

	@SuppressWarnings("unchecked")
	public Map<String, IFieldValueHolder<String>> getFieldValueHolders() {
		Map<String, IFieldValueHolder<String>> result = new HashMap<String, IFieldValueHolder<String>>();
		for (FieldDescriptor descriptor : fieldDescriptors) {
			String fieldId = descriptor.getFieldId();
			IFieldValueHolder fieldValueHolder = getFieldValueHolder(new SimpleFieldIdentifier(fieldId));
			result.put(fieldId, fieldValueHolder);
		}
		return result;
	}

	/**
	 * Creates a resource selection dialog rooted at the given element.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param rootElement
	 *            the root element to populate this dialog with
	 * @param message
	 *            the message to be displayed at the top of this dialog, or <code>null</code> to display a default message
	 */
	public FieldDescriptorsBasedPromptDialog(Shell parentShell, String title, List<FieldDescriptor> fieldDescriptors) {
		super(parentShell, title, computeColumnsFromFieldDescriptors(fieldDescriptors));
		this.fieldDescriptors = Collections.unmodifiableList(fieldDescriptors);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	private static int computeColumnsFromFieldDescriptors(List<FieldDescriptor> fieldDescriptors) {
		int result = 2;
		for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
			if (fieldDescriptor.getFieldType() == FieldType.FILE_CHOICE || fieldDescriptor.getFieldType() == FieldType.FOLDER_CHOICE) {
				result = 3;
				break;
			}
		}
		return result;
	}

	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
			IFieldValueHolder<String> fieldValueHolder = new StringHolder(new SimpleFieldIdentifier(fieldDescriptor.getFieldId()));
			fieldValueHolder.setValue(fieldDescriptor.getDefaultValue());
			if (fieldDescriptor.getFieldType() == FieldType.TEXT) {
				createTextField(composite, fieldValueHolder, fieldDescriptor.getFieldLabel(), !fieldDescriptor.getReadOnly());
			}
			if (fieldDescriptor.getFieldType() == FieldType.TEXT_AREA) {
				createTextAreaField(composite, fieldValueHolder, fieldDescriptor.getFieldLabel(), !fieldDescriptor.getReadOnly(), 3);
			}
			if (fieldDescriptor.getFieldType() == FieldType.FILE_CHOICE) {
				createFileChoiceField(composite, fieldValueHolder, fieldDescriptor.getFieldLabel(), !fieldDescriptor.getReadOnly(), new String[] {});
			}
			if (fieldDescriptor.getFieldType() == FieldType.FOLDER_CHOICE) {
				createFolderChoiceField(composite, fieldValueHolder, fieldDescriptor.getFieldLabel(), !fieldDescriptor.getReadOnly());
			}
		}
		return composite;
	}

	public String getFieldValue(String fieldId) {
		IFieldValueHolder<String> fieldValueHolder = getFieldValueHolders().get(fieldId);
		return fieldValueHolder==null?null:fieldValueHolder.getValue();
	}
}
