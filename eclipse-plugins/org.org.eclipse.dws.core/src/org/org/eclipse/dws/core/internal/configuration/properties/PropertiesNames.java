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


/**
 * The Class PropertiesNames.
 * 
 * @author pagregoire
 */
public final class PropertiesNames {
	
	/** The Constant P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD. */
	public static final String P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD = PropertiesNames.class.getName() + ".undeterminedToClasspath";

	/** The Constant P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE. */
	public static final String P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE = PropertiesNames.class.getName() + ".conflictingAutomaticallyRemove";

	/** The Constant P_MAVEN_POM_FILE_NAMES. */
	public static final String P_MAVEN_POM_FILE_NAMES = PropertiesNames.class.getName() + ".mavenPomFileNames";

	/** The Constant P_MAVEN_WEBAPP_FOLDER. */
	public static final String P_MAVEN_WEBAPP_FOLDER = PropertiesNames.class.getName() + ".mavenDefaultFolder";

	/** The Constant P_MAVEN_FOLDER. */
	public static final String P_MAVEN_FOLDER = PropertiesNames.class.getName() + ".mavenDefaultFolder2";

	/** The Constant P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD. */
	public static final String P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD = PropertiesNames.class.getName() + ".transitiveDependenciesToClasspath";

	/** The Constant P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES. */
	public static final String P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES = PropertiesNames.class.getName() + ".dealWithTransitiveDependencies";

	/** The Constant P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE. */
	public static final String P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE = PropertiesNames.class.getName() + ".dealWithDependenciesOfUndeterminedOrRestrictiveScope";

	/** The Constant P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES. */
	public static final String P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES = PropertiesNames.class.getName() + ".considerOptional";

	/** The Constant P_USE_WORSPACE_PREFERENCES. */
	public static final String P_USE_WORSPACE_PREFERENCES = PropertiesNames.class.getName() + ".useWorkspacePreferences";

	/** The Constant P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES. */
	public static final String P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES = PropertiesNames.class.getName() + ".projectSkippedDependencies";

	/** The Constant P_MAVEN_USE_LIBRARY_CONTAINER. */
	public static final String P_MAVEN_USE_LIBRARY_CONTAINER = PropertiesNames.class.getName() + ".useLibraryContainer";

	// public static final String P_MAVEN_USE_VARIABLE = PropertiesNames.class.getName() + ".useVariable";

	/** The Constant P_MAVEN_VARIABLE_NAME. */
	public static final String P_MAVEN_VARIABLE_NAME = PropertiesNames.class.getName() + ".variableName";

	/** The Constant P_MAVEN_APPEND_REPOSITORY_NAME. */
	public static final String P_MAVEN_APPEND_REPOSITORY_NAME = PropertiesNames.class.getName() + ".appendRepositoryName";

	/** The Constant P_MAVEN_FILTERED_CONTAINERS. */
	public static final String P_MAVEN_FILTERED_CONTAINERS = PropertiesNames.class.getName() + ".filteredContainers";

	/** The Constant P_MAVEN_FILTERED_JARS. */
	public static final String P_MAVEN_FILTERED_JARS = PropertiesNames.class.getName() + ".filteredJars";

	/** The Constant P_MAVEN_POM_FILE_ENCODING. */
	public static final String P_MAVEN_POM_FILE_ENCODING = PropertiesNames.class.getName() + ".mavenPomFileEncoding";

	/** The Constant P_MAVEN_PROJECT_POM_PROPERTIES. */
	public static final String P_MAVEN_PROJECT_POM_PROPERTIES = PropertiesNames.class.getName() + ".projectPomProperties";

}