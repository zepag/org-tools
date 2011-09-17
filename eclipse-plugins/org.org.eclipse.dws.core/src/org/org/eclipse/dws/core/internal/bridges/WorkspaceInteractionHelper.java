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
package org.org.eclipse.dws.core.internal.bridges;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;


/**
 * The Class WorkspaceInteractionHelper.
 */
public class WorkspaceInteractionHelper {
	
	/**
	 * Compute project names.
	 * 
	 * @param natureIds the nature ids
	 * 
	 * @return the string[]
	 */
	public static String[] computeProjectNames(String... natureIds) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		List<String> projectNamesList = new ArrayList<String>();
		for (IProject project : projects) {
			try {
				if (natureIds != null && natureIds.length > 0) {
					for (String natureId : natureIds) {
						if (project.isAccessible() && project.hasNature(natureId)) {
							projectNamesList.add(project.getName());
						}
					}
				} else {
					projectNamesList.add(project.getName());
				}
			} catch (CoreException ce) {
				// do something deeply meaningful here ;)
			}
		}
		return projectNamesList.toArray(new String[0]);
	}
}
