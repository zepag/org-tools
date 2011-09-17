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

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
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

public class GeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Instantiates a new general preference page.
	 */
	public GeneralPreferencePage() {
		super(GRID);
		setImageDescriptor(DWSCorePlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_HTTP_REPOSITORY_16));
		setPreferenceStore(DWSCorePlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages.GeneralPreferencePage_description);
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
		class DirectoryFieldEditorWithoutValidation extends DirectoryFieldEditor {

			public DirectoryFieldEditorWithoutValidation(String name, String labelText, Composite parent) {
				super(name, labelText, parent);
			}

			@Override
			protected boolean checkState() {
				// don't validate the repository. if it doesn't exist, it will be created on the fly.
				return true;
			}
		}
		addField(new DirectoryFieldEditorWithoutValidation(PreferencesNames.P_MAVEN_REPOSITORY_LOCAL_PATH, PreferencesMessages.GeneralPreferencePage_artifactDownloadPath, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferencesNames.P_MAVEN_POM_FILE_NAMES, PreferencesMessages.GeneralPreferencePage_commaSeparatedPomNames, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferencesNames.P_MAVEN_POM_FILE_ENCODING, "Pom file encoding:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferencesNames.P_MAVEN_DEFAULT_WEBAPP_FOLDER, PreferencesMessages.GeneralPreferencePage_defaultWEBINFLIB, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferencesNames.P_MAVEN_DEFAULT_FOLDER, PreferencesMessages.GeneralPreferencePage_defaultProjectFolder, getFieldEditorParent()));
		addField(new StringFieldEditor(PreferencesNames.P_MAVEN_VARIABLE_NAME, PreferencesMessages.GeneralPreferencePage_variableName, getFieldEditorParent()));
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}