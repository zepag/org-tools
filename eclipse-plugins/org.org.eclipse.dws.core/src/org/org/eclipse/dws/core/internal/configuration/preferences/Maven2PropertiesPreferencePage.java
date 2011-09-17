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
package org.org.eclipse.dws.core.internal.configuration.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.org.eclipse.core.utils.platform.preferences.PreferencesFacade;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.images.PluginImages;


/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing <samp>FieldEditorPreferencePage </samp>, we can use the field support built into JFace that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class Maven2PropertiesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Instantiates a new maven2 properties preference page.
	 */
	public Maven2PropertiesPreferencePage() {
		super(GRID);
		setImageDescriptor(DWSCorePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_HTTP_REPOSITORY_16));
		setPreferenceStore(DWSCorePlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages.AdvancedPreferencePage_description);
		initializeDefaults();
	}

	/**
	 * Sets the default values of the preferences.
	 */
	private void initializeDefaults() {
		PreferencesFacade.initDefaultValues(DWSCorePlugin.getDefault(), true);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various types of preferences. Each field editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addLabel(PreferencesMessages.AdvancedPreferencePage_considerOptionalDescription, getFieldEditorParent());
		addField(new BooleanFieldEditor(PreferencesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, PreferencesMessages.AdvancedPreferencePage_considerOptional, getFieldEditorParent()));
		addLabel(PreferencesMessages.AdvancedPreferencePage_dealWithTransitiveDescription, getFieldEditorParent());
		addField(new BooleanFieldEditor(PreferencesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, PreferencesMessages.AdvancedPreferencePage_dealWithTransitive, getFieldEditorParent()));
		addLabel(PreferencesMessages.AdvancedPreferencePage_dealWithUndeterminedDescription, getFieldEditorParent());
		addField(new BooleanFieldEditor(PreferencesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, PreferencesMessages.AdvancedPreferencePage_dealWithUndetermined, getFieldEditorParent()));
		addLabel(PreferencesMessages.AdvancedPreferencePage_automaticallyAddUndeterminedDescription, getFieldEditorParent());
		addField(new BooleanFieldEditor(PreferencesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, PreferencesMessages.AdvancedPreferencePage_automaticallyAddUndetermined, getFieldEditorParent()));
		addLabel(PreferencesMessages.AdvancedPreferencePage_automaticallyRemoveConflictingDescription, getFieldEditorParent());
		addField(new BooleanFieldEditor(PreferencesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, PreferencesMessages.AdvancedPreferencePage_automaticallyRemoveConflicting, getFieldEditorParent()));
		addLabel(PreferencesMessages.AdvancedPreferencePage_automaticallyAddTransitiveDescription, getFieldEditorParent());
		addField(new BooleanFieldEditor(PreferencesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, PreferencesMessages.AdvancedPreferencePage_automaticallyAddTransitive, getFieldEditorParent()));
	}

	/**
	 * Adds the label.
	 * 
	 * @param string the string
	 * @param composite the composite
	 */
	private void addLabel(String string, Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);

		label.setForeground(getFieldEditorParent().getDisplay().getSystemColor(SWT.COLOR_BLUE));
		label.setText(string);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}