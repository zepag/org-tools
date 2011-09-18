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

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;


/**
 * @author pagregoire
 */
public final class JVMHelper {

    private JVMHelper() {
    }

    public static IPath getDefaultJVMPath() {
        final IVMInstall defaultVMInstall = JavaRuntime.getDefaultVMInstall();
        final File installLocation = defaultVMInstall.getInstallLocation();
        return new Path(installLocation.getAbsolutePath());
    }

    public static IPath getJavaProjectJVMPath(IJavaProject javaProject) {
        IPath result = null;
        String installLocation = null;
        try {
            IVMInstall vmInstall = JavaRuntime.getVMInstall(javaProject);
            installLocation = vmInstall.getInstallLocation().getAbsolutePath();
            result = new Path(installLocation);
        } catch (Exception exc) {
            result = getDefaultJVMPath();
        }
        return result;
    }
}