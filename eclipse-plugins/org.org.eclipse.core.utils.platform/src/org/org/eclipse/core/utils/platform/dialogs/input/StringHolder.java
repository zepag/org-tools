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

public class StringHolder implements IFieldValueHolder<String> {
	private String value = "";
	private IFieldIdentifier fieldId;

	public StringHolder(IFieldIdentifier fieldId) {
		this.fieldId = fieldId;
	}

	public StringHolder(IFieldIdentifier fieldId, String value) {
		this.fieldId = fieldId;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public IFieldIdentifier getFieldId() {
		return fieldId;
	}
}
