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
package org.org.eclipse.dws.core.internal.configuration.preferences.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.preferences.editor.MapEditor;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationConstants;
import org.org.eclipse.dws.core.internal.dialogs.PropertyPromptDialog;


/**
 * A field editor to edit directory paths.
 */
public class PropertiesFieldEditor extends MapEditor {

	/**
	 * Creates a new path field editor.
	 */
	protected PropertiesFieldEditor() {
	}

	/**
	 * Instantiates a new properties field editor.
	 * 
	 * @param name the name
	 * @param labelText the label text
	 * @param urlChooserLabelText the url chooser label text
	 * @param parent the parent
	 */
	public PropertiesFieldEditor(String name, String labelText, String urlChooserLabelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	/* (non-Javadoc)
	 * @see org.org.eclipse.core.utils.platform.preferences.editor.MapEditor#createListPreference(java.util.Set)
	 */
	/**
	 * @see org.org.eclipse.core.utils.platform.preferences.editor.MapEditor#createListPreference(java.util.Set)
	 */
	@Override
	protected String createListPreference(Set<Entry> entries) {
		StringBuilder path = new StringBuilder("");//$NON-NLS-1$
		for (Entry entry : entries) {
			path.append(entry.getKey() + "=" + entry.getValue());
			path.append(ConfigurationConstants.PIPE_SEPARATOR); 
		}
		return path.toString();
	}

	/* (non-Javadoc)
	 * @see org.org.eclipse.core.utils.platform.preferences.editor.MapEditor#getNewInputObject()
	 */
	/**
	 * @see org.org.eclipse.core.utils.platform.preferences.editor.MapEditor#getNewInputObject()
	 */
	@Override
	protected Entry getNewInputObject() {
		PropertyPromptDialog stringInputDialog = new PropertyPromptDialog(getShell()); 
		stringInputDialog.setValidator(new IFieldsValidator() {

			@SuppressWarnings("unchecked")
			public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
				StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
				IFieldValueHolder<String> fieldValueHolder = fieldValueHolders.get(PropertyPromptDialog.PROPERTY_KEY_FIELD);
				String key = fieldValueHolder.getValue();
				if (key.trim().equals("")) {
					validationResult.append("Key cannot be empty");
				}
				return validationResult;
			}

		});
		if (stringInputDialog.open() == Window.CANCEL) {
			return null;
		}
		return new Entry(stringInputDialog.getPropertyKey(), stringInputDialog.getPropertyValue());
	}

	/* (non-Javadoc)
	 * @see org.org.eclipse.core.utils.platform.preferences.editor.MapEditor#parseString(java.lang.String)
	 */
	/**
	 * @see org.org.eclipse.core.utils.platform.preferences.editor.MapEditor#parseString(java.lang.String)
	 */
	@Override
	protected Map<String, String> parseString(String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, ConfigurationConstants.PIPE_SEPARATOR); 
		Map<String, String> v = new HashMap<String, String>();
		while (st.hasMoreTokens()) {
			StringTokenizer tkz = new StringTokenizer(st.nextToken(), "=", false);
			String key = tkz.nextToken();
			String value = tkz.nextToken();
			v.put(key, value);
		}
		return v;
	}
}