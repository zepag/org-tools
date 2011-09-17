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
package org.org.eclipse.core.utils.platform.tools;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ProjectHelper {
	public static void doRefresh(IProject project, IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Refreshing project " + project.getName());
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		monitor.worked(1);
	}

	public static void checkProjectStatus(IProject project, IProgressMonitor monitor) throws CoreException {
		if (project.exists() && !project.isSynchronized(IProject.DEPTH_INFINITE)) {
			ProjectHelper.doRefresh(project, monitor);
		}
	}
}
