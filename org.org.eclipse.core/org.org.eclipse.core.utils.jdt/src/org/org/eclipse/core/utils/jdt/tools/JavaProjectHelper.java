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
package org.org.eclipse.core.utils.jdt.tools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author pagregoire
 */
public final class JavaProjectHelper {

	private JavaProjectHelper() {
	}

	public static boolean areThereJavaProjects() {
		boolean result = false;
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			try {
				if (isJavaProject(project)) {
					result = true;
					break;
				}
			} catch (CoreException e) {
				// project is probably not a java project
			}
		}
		return result;
	}

	public static List<IJavaProject> getJavaProjects() {
		List<IJavaProject> result = new ArrayList<IJavaProject>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			try {
				if (isJavaProject(project)) {
					result.add(JavaCore.create(project));
				}
			} catch (CoreException e) {
				// project is probably not a java project
			}
		}
		return result;
	}

	public static boolean isJavaProject(IProject project) throws CoreException {
		return project.hasNature(JavaCore.NATURE_ID);
	}

	
}