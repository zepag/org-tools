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
package org.org.eclipse.core.utils.platform.preferences;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author pagregoire
 */
public class HiddenFolderHandler {
    public static void checkGlobalHiddenFolder(String projectHiddenFolderName,IProject project, IProgressMonitor monitor) {
        try {
            IFolder folder = project.getFolder(projectHiddenFolderName);
            if (!folder.exists()) {
                folder.create(IFolder.FORCE, true, monitor);
            }
        } catch (CoreException e) {
            throw new org.org.eclipse.core.utils.platform.PlatformUtilsException(e);
        }
    }
}
