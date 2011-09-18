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
package org.org.eclipse.dws.core.internal.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.org.eclipse.core.utils.platform.preferences.PreferencesFacade;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.configuration.preferences.PreferencesNames;
import org.org.eclipse.dws.core.internal.configuration.properties.PropertiesFacade;
import org.org.eclipse.dws.core.internal.configuration.properties.PropertiesNames;
import org.org.eclipse.dws.core.internal.model.SkippedDependency;

/**
 * The Class AggregatedProperties.
 */
public final class AggregatedProperties {

	/**
	 * Instantiates a new aggregated properties.
	 */
	private AggregatedProperties() {
	}

	/**
	 * Use workspace preferences.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return true, if successful
	 */
	public static boolean useWorkspacePreferences(IProject project) {
		Object usePreferencesProperty = getProjectProperty(project, PropertiesNames.P_USE_WORSPACE_PREFERENCES);
		boolean result = true;
		if (usePreferencesProperty != null) {
			result = Boolean.valueOf(usePreferencesProperty.toString());
		}
		return result;
	}

	/**
	 * Gets the workspace preference.
	 * 
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * 
	 * @return the workspace preference
	 */
	private static Object getWorkspacePreference(String name, Class<?> clazz) {
		return PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), name, clazz);
	}

	/**
	 * Gets the project property.
	 * 
	 * @param project
	 *            the project
	 * @param name
	 *            the name
	 * 
	 * @return the project property
	 */
	private static Object getProjectProperty(IProject project, String name) {
		return PropertiesFacade.getProjectProperty(project, name, PropertiesFacade.IGNORE_NULL);
	}

	/**
	 * Gets the boolean property.
	 * 
	 * @param project
	 *            the project
	 * @param workspacePreferenceName
	 *            the workspace preference name
	 * @param projectPropertyName
	 *            the project property name
	 * 
	 * @return the boolean property
	 */
	private static Boolean getBooleanProperty(IProject project, String workspacePreferenceName, String projectPropertyName) {
		Boolean result = null;
		if (!checkProject(project) || useWorkspacePreferences(project)) {
			result = (Boolean) getWorkspacePreference(workspacePreferenceName, Boolean.class);
		} else {
			result = Boolean.valueOf((String) getProjectProperty(project, projectPropertyName));
		}
		return result;
	}

	/**
	 * Gets the string property.
	 * 
	 * @param project
	 *            the project
	 * @param workspacePreferenceName
	 *            the workspace preference name
	 * @param projectPropertyName
	 *            the project property name
	 * 
	 * @return the string property
	 */
	private static String getStringProperty(IProject project, String workspacePreferenceName, String projectPropertyName) {
		String result = null;
		if (!checkProject(project) || useWorkspacePreferences(project)) {
			result = (String) getWorkspacePreference(workspacePreferenceName, String.class);
		} else {
			result = (String) getProjectProperty(project, projectPropertyName);
		}
		return result;
	}

	/**
	 * Check project.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return true, if successful
	 */
	private static boolean checkProject(IProject project) {
		boolean result = true;
		if (project != null) {
			result = result && project.exists();
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Gets the pom file names.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the pom file names
	 */
	public static String getPomFileNames(IProject project) {
		return getStringProperty(project, PreferencesNames.P_MAVEN_POM_FILE_NAMES, PropertiesNames.P_MAVEN_POM_FILE_NAMES);
	}

	/**
	 * Gets the deal with narrow.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the deal with narrow
	 */
	public static Boolean getDealWithNarrow(IProject project) {
		return getBooleanProperty(project, PreferencesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, PropertiesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE);
	}

	/**
	 * Gets the deal with optional.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the deal with optional
	 */
	public static Boolean getDealWithOptional(IProject project) {
		return getBooleanProperty(project, PreferencesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, PropertiesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES);
	}

	/**
	 * Gets the local repository.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the local repository
	 */
	public static String getLocalRepository(IProject project) {
		return (String) getWorkspacePreference(PreferencesNames.P_MAVEN_REPOSITORY_LOCAL_PATH, String.class);
	}

	/**
	 * Gets the deal with transitive.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the deal with transitive
	 */
	public static Boolean getDealWithTransitive(IProject project) {
		return getBooleanProperty(project, PreferencesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, PropertiesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES);
	}

	/**
	 * Gets the default lib folder.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the default lib folder
	 */
	public static String getDefaultLibFolder(IProject project) {
		return getStringProperty(project, PreferencesNames.P_MAVEN_DEFAULT_FOLDER, PropertiesNames.P_MAVEN_FOLDER);
	}

	/**
	 * Gets the automatically add transitive.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the automatically add transitive
	 */
	public static Boolean getAutomaticallyAddTransitive(IProject project) {
		return getBooleanProperty(project, PreferencesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, PropertiesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD);
	}

	/**
	 * Gets the automatically remove conflicting.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the automatically remove conflicting
	 */
	public static Boolean getAutomaticallyRemoveConflicting(IProject project) {
		return getBooleanProperty(project, PreferencesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, PropertiesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE);
	}

	/**
	 * Gets the automatically add unknown.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the automatically add unknown
	 */
	public static Boolean getAutomaticallyAddUnknown(IProject project) {
		return getBooleanProperty(project, PreferencesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, PropertiesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD);
	}

	/**
	 * Gets the web app folder.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the web app folder
	 */
	public static String getWebAppFolder(IProject project) {
		return getStringProperty(project, PreferencesNames.P_MAVEN_DEFAULT_WEBAPP_FOLDER, PropertiesNames.P_MAVEN_WEBAPP_FOLDER);
	}

	/**
	 * Gets the skipped dependencies.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the skipped dependencies
	 */
	public static Set<SkippedDependency> getSkippedDependencies(IProject project) {
		Set<SkippedDependency> result = new HashSet<SkippedDependency>();
		if (project != null) {
			Object property = getProjectProperty(project, PropertiesNames.P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES);
			if (property != null) {
				StringTokenizer tkz = new StringTokenizer((String) property, "|");
				StringTokenizer tkz2 = null;
				while (tkz.hasMoreTokens()) {
					tkz2 = new StringTokenizer(tkz.nextToken(), ",");
					result.add(new SkippedDependency(tkz2.nextToken(), tkz2.nextToken()));
				}
			}
		}
		return result;
	}

	/**
	 * Gets the use classpath container.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the use classpath container
	 */
	@Deprecated
	public static Boolean getUseClasspathContainer(IProject project) {
		return getBooleanProperty(project, PreferencesNames.P_MAVEN_USE_LIBRARY_CONTAINER, PropertiesNames.P_MAVEN_USE_LIBRARY_CONTAINER);
	}

	/**
	 * Gets the append repository name.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the append repository name
	 * @deprecated
	 */
	@Deprecated
	public static Boolean getAppendRepositoryName(IProject project) {
		return getBooleanProperty(project, PreferencesNames.P_MAVEN_APPEND_REPOSITORY_NAME, PropertiesNames.P_MAVEN_APPEND_REPOSITORY_NAME);
	}

	/**
	 * Gets the variable name.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the variable name
	 */
	public static String getVariableName(IProject project) {
		return getStringProperty(project, PreferencesNames.P_MAVEN_VARIABLE_NAME, PropertiesNames.P_MAVEN_VARIABLE_NAME);
	}

	/**
	 * Gets the wizard filtered jars.
	 * 
	 * @return the wizard filtered jars
	 */
	public static String getWizardFilteredJars() {
		return (String) getWorkspacePreference(PreferencesNames.P_MAVEN_FILTERED_LIBS, String.class);
	}

	/**
	 * Gets the hide approximative match.
	 * 
	 * @return the hide approximative match
	 */
	public static Boolean getHideApproximativeMatch() {
		return (Boolean) getWorkspacePreference(PreferencesNames.P_MAVEN_HIDE_APPROXIMATIVE_MATCH, Boolean.class);
	}

	/**
	 * Gets the number of kept matches.
	 * 
	 * @return the number of kept matches
	 */
	public static Integer getNumberOfKeptMatches() {
		return (Integer) getWorkspacePreference(PreferencesNames.P_MAVEN_NUMBER_OF_KEPT_MATCHES, Integer.class);
	}

	/**
	 * Gets the artifact extensions.
	 * 
	 * @return the artifact extensions
	 */
	public static Set<String> getArtifactExtensions() {
		return getArtifactExtensions((String) getWorkspacePreference(PreferencesNames.P_MAVEN_ARTIFACT_EXTENSIONS, String.class));
	}

	/**
	 * Gets the artifact extensions.
	 * 
	 * @param pref
	 *            the pref
	 * 
	 * @return the artifact extensions
	 */
	private static Set<String> getArtifactExtensions(String pref) {
		StringTokenizer tkz = new StringTokenizer(pref, "|,; ", false);
		Set<String> set = new LinkedHashSet<String>();
		while (tkz.hasMoreTokens()) {
			set.add(tkz.nextToken());
		}
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Gets the pom encoding.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the pom encoding
	 */
	public static String getPomEncoding(IProject project) {
		return getStringProperty(project, PreferencesNames.P_MAVEN_POM_FILE_ENCODING, PropertiesNames.P_MAVEN_POM_FILE_ENCODING);
	}

	/**
	 * Gets the pom properties.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return the pom properties
	 */
	public static Map<String, String> getPomProperties(IProject project) {
		Map<String, String> properties = new HashMap<String, String>();
		if (project != null) {
			properties.put("basedir", project.getLocation().makeAbsolute().toOSString());
		}
		return properties;
	}

	/**
	 * Format skipped dependencies.
	 * 
	 * @param skippedDependencies
	 *            the skipped dependencies
	 * 
	 * @return the string
	 */
	public static String formatSkippedDependencies(Set<SkippedDependency> skippedDependencies) {
		StringBuilder result = new StringBuilder();
		for (SkippedDependency skippedDependency : skippedDependencies) {
			result.append(skippedDependency.getGroupId() + "," + skippedDependency.getArtifactId() + "|");
		}
		return result.toString();
	}

}
