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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.org.eclipse.core.utils.platform.PlatformUtilsException;
import org.org.eclipse.core.utils.platform.PlatformUtilsPlugin;
import org.osgi.framework.Bundle;

/**
 * @author pagregoire
 */
public final class PluginToolBox {

	private PluginToolBox() {
	}

	public static IWorkspace getCurrentWorkspace() {
		IWorkspace returnIWorkspace = ResourcesPlugin.getWorkspace();
		return returnIWorkspace;
	}

	public static IWorkbench getWorkbench() {
		IWorkbench returnIWorkbench = PlatformUtilsPlugin.getDefault().getWorkbench();
		return returnIWorkbench;
	}

	public static Object getAdapted(Object toAdapt, Class<?> targetClass) {
		return ((IAdaptable) toAdapt).getAdapter(targetClass);
	}

	public static File getFileFromBundle(Bundle bundle, String relativePath) {
		File result = null;
		try {
			URL eclipseURL = FileLocator.find(bundle, new Path(relativePath), null);
			if (eclipseURL != null) {
				URL url = FileLocator.resolve(eclipseURL);
				// patch for spaces in the URL (Microsoft paths)
				String urlPath = url.toExternalForm().replaceAll(" ", "%20");
				result = new File(new URI(urlPath));
			} else {
				throw new PlatformUtilsException("Impossible to find resource with name \"" + relativePath + "\" in bundle " + bundle.getSymbolicName());
			}
		} catch (URISyntaxException use) {
			throw new PlatformUtilsException("Bad URI Syntax", use);
		} catch (IOException ioe) {
			throw new PlatformUtilsException("IO Problem", ioe);
		}
		return result;
	}

	public static InputStream getStream(Plugin plugin, String relativePath) {
		InputStream result = null;
		try {
			result = FileLocator.openStream(plugin.getBundle(), new Path(relativePath), false);
		} catch (IOException ioe) {
			throw new PlatformUtilsException("IO Problem", ioe);
		}
		return result;
	}

	public static Shell getActiveShell(AbstractUIPlugin plugin) {
		if (plugin == null)
			return null;
		IWorkbench workBench = plugin.getWorkbench();
		if (workBench == null)
			return null;
		IWorkbenchWindow workBenchWindow = workBench.getActiveWorkbenchWindow();
		if (workBenchWindow == null)
			return null;
		return workBenchWindow.getShell();
	}

	public static URL getPluginInstallationURL(AbstractUIPlugin plugin) {
		URL result = null;
		try {
			result = FileLocator.resolve(FileLocator.find(plugin.getBundle(), new Path(""), null));
		} catch (IOException ioe) {
			throw new PlatformUtilsException("IO Problem", ioe);
		}
		return result;
	}

	public static IProject getProject(IStructuredSelection selection) {
		IProject project = null;
		IResource tmpFile = null;
		if (selection instanceof IStructuredSelection) {
			Object selectedElement = ((IStructuredSelection) selection).getFirstElement();
			if (selectedElement instanceof IResource) {
				tmpFile = (IResource) selectedElement;
				project = tmpFile.getProject();
			}
		}
		return project;
	}
}