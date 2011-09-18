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


/**
 * The Class PreferencesNames.
 * 
 * @author pagregoire
 */
public final class PreferencesNames {

	/** The Constant P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD. */
	public static final String P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD = PreferencesNames.class.getName() + ".undeterminedToClasspath";

	/** The Constant P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE. */
	public static final String P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE = PreferencesNames.class.getName() + ".conflictingAutomaticallyRemove";

	/** The Constant P_MAVEN_REPOSITORIES_MODEL. */
	public static final String P_MAVEN_REPOSITORIES_MODEL = PreferencesNames.class.getName() + ".mavenRepositoriesModel";

	/** The Constant P_MAVEN_REPOSITORIES_INFOS. */
	public static final String P_MAVEN_REPOSITORIES_INFOS = PreferencesNames.class.getName() + ".mavenRepositoriesInfos";

	/** The Constant P_MAVEN_REPOSITORY_LOCAL_PATH. */
	public static final String P_MAVEN_REPOSITORY_LOCAL_PATH = PreferencesNames.class.getName() + ".mavenRepositoryLocalPath";

	/** The Constant P_MAVEN_POM_FILE_NAMES. */
	public static final String P_MAVEN_POM_FILE_NAMES = PreferencesNames.class.getName() + ".mavenPomFileNames";

	/** The Constant P_MAVEN_DEFAULT_WEBAPP_FOLDER. */
	public static final String P_MAVEN_DEFAULT_WEBAPP_FOLDER = PreferencesNames.class.getName() + ".mavenDefaultFolder";

	/** The Constant P_MAVEN_DEFAULT_FOLDER. */
	public static final String P_MAVEN_DEFAULT_FOLDER = PreferencesNames.class.getName() + ".mavenDefaultFolder2";

	/** The Constant P_MAVEN_HTTP_REPOSITORIES_AUTOCOMPLETE. */
	public static final String P_MAVEN_HTTP_REPOSITORIES_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompleteHttpRepository";

	/** The Constant P_MAVEN_REPOSITORY_NAME_AUTOCOMPLETE. */
	public static final String P_MAVEN_REPOSITORY_NAME_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompleteRepositoryName";

	/** The Constant P_MAVEN_FS_REPOSITORIES_AUTOCOMPLETE. */
	public static final String P_MAVEN_FS_REPOSITORIES_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompleteFsRepository";

	/** The Constant P_MAVEN_ARTIFACT_EXTENSIONS. */
	public static final String P_MAVEN_ARTIFACT_EXTENSIONS = PreferencesNames.class.getName() + ".artifactExtensions";

	/** The Constant P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD. */
	public static final String P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD = PreferencesNames.class.getName() + ".transitiveDependenciesToClasspath";

	/** The Constant P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES. */
	public static final String P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES = PreferencesNames.class.getName() + ".dealWithTransitiveDependencies";

	/** The Constant P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE. */
	public static final String P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE = PreferencesNames.class.getName() + ".dealWithDependenciesOfUndeterminedOrRestrictiveScope";

	/** The Constant P_MAVEN_ENTRY_PATTERN_AUTOCOMPLETE. */
	public static final String P_MAVEN_ENTRY_PATTERN_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompleteEntryPattern";

	/** The Constant P_MAVEN_DIRECTORY_ENTRY_PATTERN_AUTOCOMPLETE. */
	public static final String P_MAVEN_DIRECTORY_ENTRY_PATTERN_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompleteDirectoryEntryPattern";

	/** The Constant P_MAVEN_FILE_ENTRY_PATTERN_AUTOCOMPLETE. */
	public static final String P_MAVEN_FILE_ENTRY_PATTERN_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompletefileEntryPattern";

	/** The Constant P_MAVEN_PARENT_PATTERN_AUTOCOMPLETE. */
	public static final String P_MAVEN_PARENT_PATTERN_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompleteParentPattern";

	/** The Constant P_MAVEN_PROXY_HOST_AUTOCOMPLETE. */
	public static final String P_MAVEN_PROXY_HOST_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompleteProxyHost";

	/** The Constant P_MAVEN_PROXY_PORT_AUTOCOMPLETE. */
	public static final String P_MAVEN_PROXY_PORT_AUTOCOMPLETE = PreferencesNames.class.getName() + ".autocompleteProxyPort";

	/** The Constant P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES. */
	public static final String P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES = PreferencesNames.class.getName() + ".considerOptional";

	/** The Constant P_MAVEN_GROUPID_AUTOCOMPLETE. */
	public static final String P_MAVEN_GROUPID_AUTOCOMPLETE = PreferencesNames.class.getName() + ".groupIdAutocomplete";

	/** The Constant P_MAVEN_ARTIFACTID_AUTOCOMPLETE. */
	public static final String P_MAVEN_ARTIFACTID_AUTOCOMPLETE = PreferencesNames.class.getName() + ".artifactIdAutocomplete";

	/** The Constant P_MAVEN_PATTERNSET_DEFAULT. */
	public static final String P_MAVEN_PATTERNSET_DEFAULT = PreferencesNames.class.getName() + ".patternSetDefault";

	/** The Constant P_MAVEN_USE_LIBRARY_CONTAINER. */
	public static final String P_MAVEN_USE_LIBRARY_CONTAINER = PreferencesNames.class.getName() + ".useLibraryContainer";

	/** The Constant P_MAVEN_VARIABLE_NAME. */
	public static final String P_MAVEN_VARIABLE_NAME = PreferencesNames.class.getName() + ".variableName";

	/** The Constant P_MAVEN_APPEND_REPOSITORY_NAME. */
	public static final String P_MAVEN_APPEND_REPOSITORY_NAME = PreferencesNames.class.getName() + ".appendRepositoryName";

	/** The Constant P_MAVEN_FILTERED_LIBS. */
	public static final String P_MAVEN_FILTERED_LIBS = PreferencesNames.class.getName() + ".filteredLibs";

	/** The Constant P_MAVEN_HIDE_APPROXIMATIVE_MATCH. */
	public static final String P_MAVEN_HIDE_APPROXIMATIVE_MATCH = PreferencesNames.class.getName() + ".hideApproximativeMatches";

	/** The Constant P_MAVEN_NUMBER_OF_KEPT_MATCHES. */
	public static final String P_MAVEN_NUMBER_OF_KEPT_MATCHES = PreferencesNames.class.getName() + ".numberOfMatches";

	/** The Constant P_MAVEN_POM_FILE_ENCODING. */
	public static final String P_MAVEN_POM_FILE_ENCODING = PreferencesNames.class.getName() + ".mavenPomFileEncoding";

	/** The Constant P_MAVEN_POM_PROPERTIES. */
	public static final String P_MAVEN_POM_PROPERTIES = PreferencesNames.class.getName() + ".mavenPomProperties";

	public static final String P_MAVEN_LIBRARYPACK_NAME_AUTOCOMPLETE =  PreferencesNames.class.getName() + ".autocompleteLibraryPackName";
}