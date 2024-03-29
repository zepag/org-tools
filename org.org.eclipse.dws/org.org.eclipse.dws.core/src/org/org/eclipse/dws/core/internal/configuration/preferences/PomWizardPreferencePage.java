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
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.org.eclipse.core.utils.platform.preferences.PreferencesFacade;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.configuration.preferences.editor.LibrariesFieldEditor;
import org.org.eclipse.dws.core.internal.configuration.preferences.editor.PropertiesFieldEditor;
import org.org.eclipse.dws.core.internal.images.PluginImages;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing <samp>FieldEditorPreferencePage </samp>, we can use the field support built into JFace that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main plug-in class. That way, preferences can be accessed directly via the preference store.
 */

public class PomWizardPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Instantiates a new wizard preference page.
	 */
	public PomWizardPreferencePage() {
		super(GRID);
		setImageDescriptor(DWSCorePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_HTTP_REPOSITORY_16));
		setPreferenceStore(DWSCorePlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages.WizardPreferencePage_description);
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
		addField(new BooleanFieldEditor(PreferencesNames.P_MAVEN_HIDE_APPROXIMATIVE_MATCH, PreferencesMessages.WizardPreferencePage_onlyKeepExactMatch, getFieldEditorParent()));
		addField(new LibrariesFieldEditor(PreferencesNames.P_MAVEN_FILTERED_LIBS, PreferencesMessages.WizardPreferencePage_filteredLibraries, PreferencesMessages.WizardPreferencePage_filteredLibrariesPopup, getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferencesNames.P_MAVEN_NUMBER_OF_KEPT_MATCHES, PreferencesMessages.WizardPreferencePage_numberOfKeptMatches, getFieldEditorParent()));
		addField(new PropertiesFieldEditor(PreferencesNames.P_MAVEN_POM_PROPERTIES, "Pom properties", "Enter property in key=value format", getFieldEditorParent()));
		// addField(new LibrariesFieldEditor(PreferencesNames.P_MAVEN_FILTERED_CONTAINERS, PreferencesMessages.WizardPreferencePage_filteredJars, PreferencesMessages.WizardPreferencePage_filteredJarsPopup, getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}