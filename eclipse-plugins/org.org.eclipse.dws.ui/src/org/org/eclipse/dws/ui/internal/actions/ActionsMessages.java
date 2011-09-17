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
package org.org.eclipse.dws.ui.internal.actions;

import org.eclipse.osgi.util.NLS;


/**
 * The Class ActionsMessages.
 */
public class ActionsMessages extends NLS {

	static {
		NLS.initializeMessages(ActionsMessages.class.getName(), ActionsMessages.class);
	}

	/** The Abstract parse pom action delegate_unattended exception. */
	public static String AbstractParsePomActionDelegate_unattendedException;

	/** The Abstract parse pom action delegate_unattended exception with description. */
	public static String AbstractParsePomActionDelegate_unattendedExceptionWithDescription;

	/** The Create classpath variable action_description1. */
	public static String CreateClasspathVariableAction_description1;

	/** The Create classpath variable action_description2. */
	public static String CreateClasspathVariableAction_description2;

	/** The Create classpath variable action_id. */
	public static String CreateClasspathVariableAction_id;

	/** The Create classpath variable action_unattended exception. */
	public static String CreateClasspathVariableAction_unattendedException;

	/** The Create classpath variable action_unattended exception with description. */
	public static String CreateClasspathVariableAction_unattendedExceptionWithDescription;

	/** The Open maven repository view action_id. */
	public static String OpenMavenRepositoryViewAction_id;

	/** The Open details view action_id. */
	public static String OpenDetailsViewAction_id;

	/** The Find repositories action_id. */
	public static String FindRepositoriesAction_id;
	
	/** The Pom java synchronization action delegate_add libraries to folder. */
	public static String PomJavaSynchronizationActionDelegate_addLibrariesToFolder;

	/** The Pom java synchronization action delegate_add to classpath. */
	public static String PomJavaSynchronizationActionDelegate_addToClasspath;

	/** The Pom java synchronization action delegate_no repository defined. */
	public static String PomJavaSynchronizationActionDelegate_noRepositoryDefined;

	/** The Pom java synchronization action delegate_sync target. */
	public static String PomJavaSynchronizationActionDelegate_syncTarget;

	/** The Pom java synchronization action delegate_undetermined. */
	public static String PomJavaSynchronizationActionDelegate_undetermined;

	/** The Pom web app synchronization action delegate_name. */
	public static String PomWebAppSynchronizationActionDelegate_name;

	/** The Pom web app synchronization action delegate_no repository defined. */
	public static String PomWebAppSynchronizationActionDelegate_noRepositoryDefined;
}
