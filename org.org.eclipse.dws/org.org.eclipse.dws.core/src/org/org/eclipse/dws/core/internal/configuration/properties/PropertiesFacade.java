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
package org.org.eclipse.dws.core.internal.configuration.properties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.org.eclipse.core.utils.platform.preferences.PreferencesFacade;
import org.org.eclipse.core.utils.platform.tools.PropertiesToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.configuration.preferences.PreferencesNames;


/**
 * The Class PropertiesFacade.
 * 
 * @author pagregoire
 */
public class PropertiesFacade {

	/** The Constant FAIL_IF_NULL. */
	public static final Boolean FAIL_IF_NULL = true;

	/** The Constant IGNORE_NULL. */
	public static final Boolean IGNORE_NULL = false;

	/** The default values. */
	private static Map<String, String> defaultValues;

	/** The Constant DEFAULT_CLASSPATH. */
	public final static String DEFAULT_CLASSPATH = "[DEFAULT CLASSPATH]";
	static {
		initDefaults();
	}

	/**
	 * Inits the defaults.
	 */
	public static void initDefaults() {
		defaultValues = new HashMap<String, String>();
		defaultValues.put(PropertiesNames.P_USE_WORSPACE_PREFERENCES,"true");
		defaultValues.put(PropertiesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, Boolean.class));
		defaultValues.put(PropertiesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, Boolean.class));
		defaultValues.put(PropertiesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, Boolean.class));
		defaultValues.put(PropertiesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, Boolean.class));
		defaultValues.put(PropertiesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, Boolean.class));
		defaultValues.put(PropertiesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, Boolean.class));
		defaultValues.put(PropertiesNames.P_MAVEN_FOLDER, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_DEFAULT_FOLDER, String.class));
		defaultValues.put(PropertiesNames.P_MAVEN_WEBAPP_FOLDER, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_DEFAULT_WEBAPP_FOLDER, String.class));
		defaultValues.put(PropertiesNames.P_MAVEN_POM_FILE_NAMES, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_POM_FILE_NAMES, String.class));
		defaultValues.put(PropertiesNames.P_MAVEN_POM_FILE_ENCODING, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_POM_FILE_ENCODING, String.class));
		defaultValues.put(PropertiesNames.P_MAVEN_USE_LIBRARY_CONTAINER, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_USE_LIBRARY_CONTAINER, Boolean.class));
		// defaultValues.put(PropertiesNames.P_MAVEN_USE_VARIABLE, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_USE_VARIABLE, Boolean.class));
		defaultValues.put(PropertiesNames.P_MAVEN_VARIABLE_NAME, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_VARIABLE_NAME, String.class));
		defaultValues.put(PropertiesNames.P_MAVEN_APPEND_REPOSITORY_NAME, PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_APPEND_REPOSITORY_NAME, String.class));
		defaultValues.put(PropertiesNames.P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES, "");
	}

	/**
	 * Gets the project property.
	 * 
	 * @param project the project
	 * @param propertyName the property name
	 * @param failIfNull the fail if null
	 * 
	 * @return the project property
	 */
	public static String getProjectProperty(IProject project, String propertyName, boolean failIfNull) {
		return PropertiesToolBox.getProjectProperty(defaultValues, project, propertyName, failIfNull);
	}

	/**
	 * Gets the default property value.
	 * 
	 * @param propertyName the property name
	 * 
	 * @return the default property value
	 */
	public static String getDefaultPropertyValue(String propertyName) {
		return PropertiesToolBox.getDefaultPropertyValue(defaultValues, propertyName);
	}

	/**
	 * Sets the default property value.
	 * 
	 * @param project the project
	 * @param propertyName the property name
	 * @param value the value
	 */
	public static void setDefaultPropertyValue(IProject project, String propertyName, String value) {
		PropertiesToolBox.setDefaultPropertyValue(defaultValues, project, propertyName, value);
	}

	/**
	 * Sets the project property.
	 * 
	 * @param project the project
	 * @param propertyName the property name
	 * @param value the value
	 * 
	 * @throws CoreException the core exception
	 */
	public static void setProjectProperty(IProject project, String propertyName, String value) throws CoreException {
		PropertiesToolBox.setProjectProperty(defaultValues, project, propertyName, value);
	}

	/**
	 * Inits the properties for project with default values.
	 * 
	 * @param project the project
	 */
	public static void initPropertiesForProjectWithDefaultValues(IProject project) {
		PropertiesToolBox.initPropertiesForProjectWithDefaultValues(defaultValues, project);
	}

	/**
	 * Store properties to file.
	 * 
	 * @param project the project
	 */
	public static void storePropertiesToFile(IProject project) {
		Map<String,String> properties = new HashMap<String,String>();
		properties.put(PropertiesNames.P_USE_WORSPACE_PREFERENCES, getProjectProperty(project, PropertiesNames.P_USE_WORSPACE_PREFERENCES, false));
		properties.put(PropertiesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, getProjectProperty(project, PropertiesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, false));
		properties.put(PropertiesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, getProjectProperty(project, PropertiesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, false));
		properties.put(PropertiesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, getProjectProperty(project, PropertiesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, false));
		properties.put(PropertiesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, getProjectProperty(project, PropertiesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, false));
		properties.put(PropertiesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, getProjectProperty(project, PropertiesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, false));
		properties.put(PropertiesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, getProjectProperty(project, PropertiesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, false));
		properties.put(PropertiesNames.P_MAVEN_FOLDER, getProjectProperty(project, PropertiesNames.P_MAVEN_FOLDER, false));
		properties.put(PropertiesNames.P_MAVEN_WEBAPP_FOLDER, getProjectProperty(project, PropertiesNames.P_MAVEN_WEBAPP_FOLDER, false));
		properties.put(PropertiesNames.P_MAVEN_POM_FILE_NAMES, getProjectProperty(project, PropertiesNames.P_MAVEN_POM_FILE_NAMES, false));
		properties.put(PropertiesNames.P_MAVEN_POM_FILE_ENCODING, getProjectProperty(project, PropertiesNames.P_MAVEN_POM_FILE_ENCODING, false));
		properties.put(PropertiesNames.P_MAVEN_USE_LIBRARY_CONTAINER, getProjectProperty(project, PropertiesNames.P_MAVEN_USE_LIBRARY_CONTAINER, false));
		// properties.put(PropertiesNames.P_MAVEN_USE_VARIABLE, getProjectProperty(project, PropertiesNames.P_MAVEN_USE_VARIABLE, false));
		properties.put(PropertiesNames.P_MAVEN_VARIABLE_NAME, getProjectProperty(project, PropertiesNames.P_MAVEN_VARIABLE_NAME, false));
		properties.put(PropertiesNames.P_MAVEN_APPEND_REPOSITORY_NAME, getProjectProperty(project, PropertiesNames.P_MAVEN_APPEND_REPOSITORY_NAME, false));
		properties.put(PropertiesNames.P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES, getProjectProperty(project, PropertiesNames.P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES, false));
		PropertiesPersistenceFile.saveProperties(properties, project);
	}

	/**
	 * Load properties.
	 * 
	 * @param project the project
	 */
	public static void loadProperties(IProject project) {
		Map<String,String> map = PropertiesPersistenceFile.loadProperties(project);
		try {
			if (map == null) {
				throw new CoreException(new StatusInfo());
			}
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				setProjectProperty(project, key, (String) map.get(key));
			}
		} catch (CoreException e) {
			initPropertiesForProjectWithDefaultValues(project);
		}
	}
}
