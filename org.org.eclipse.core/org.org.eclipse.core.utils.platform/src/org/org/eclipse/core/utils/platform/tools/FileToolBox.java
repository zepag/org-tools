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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.core.utils.platform.PlatformUtilsException;

/**
 * Toolbox for the purpose of files manipulations inside Eclipse PDE.
 * 
 * @author pagregoire
 */
public final class FileToolBox {
	private FileToolBox() {
	}

	/**
	 * Copy a file's content to another file.
	 * 
	 * @param in
	 *            the file to copy from
	 * @param out
	 *            the file to copy to
	 * @throws Exception
	 */
	public static void copyFile(File in, File out) throws Exception {
		FileChannel sourceChannel = new FileInputStream(in).getChannel();
		FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		sourceChannel.close();
		destinationChannel.close();
	}

	/**
	 * Gets the parent project for a system file.
	 * 
	 * @param file
	 *            a system file
	 * @return the file's project's handle
	 */
	public static IProject getParentProjectForFile(File file) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(file.getName());
		if (!project.exists()) {
			project = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath())).getProject();
		}
		return project;
	}

	/**
	 * Gets the corresponding system file from a workspace file handle.
	 * 
	 * @param eclipseFile
	 *            an eclipse file handle
	 * @return a system file
	 */
	public static File getSystemFile(IFile eclipseFile) {
		return eclipseFile.getLocation().toFile();
	}

	/**
	 * Gets the corresponding system file from a workspace path.
	 * 
	 * @param eclipsePath
	 *            an eclipse path
	 * @return a system file
	 */
	public static File getSystemFile(IPath eclipsePath) {
		return eclipsePath.toFile();
	}

	/**
	 * Gets the corresponding system file from a workspace folder handle
	 * 
	 * @param eclipseFolder
	 *            an eclipse folder handle
	 * @return a system file
	 */
	public static File getSystemFile(IFolder eclipseFolder) {
		return eclipseFolder.getLocation().toFile();
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

	public static IFile createOrUpdateFile(IProject project, String name, InputStream contents) {
		IFile file = null;
		try {
			file = project.getFile(name);
			if (file.exists()) {
				file.setContents(contents, true, true, null);
			} else {
				file.create(contents, true, null);
			}
		} catch (org.eclipse.core.runtime.CoreException e) {
			throw new PlatformUtilsException(e);
		}
		return file;
	}

	public static void createFolders(IFolder folderPath, IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Creating folder: " + folderPath.getName());
		String projectRelativePath = folderPath.getProjectRelativePath().toString();
		StringTokenizer tkz = new StringTokenizer(projectRelativePath, "/\\", false);
		IFolder folder = folderPath.getProject().getFolder(tkz.nextToken());
		if (folder.exists() == false) {
			folder.create(true, true, monitor);
		}
		while (tkz.hasMoreTokens()) {
			IFolder subFolder = folder.getFolder(tkz.nextToken());
			if (subFolder.exists() == false) {
				subFolder.create(true, true, monitor);
			}
			folder = subFolder;
		}
		monitor.worked(1);
	}

	public static IFile getFile(IProject project, String name) {
		IFile file = null;
		file = project.getFile(name);
		if (!file.exists()) {
			file = null;
		}
		return file;
	}

	public static void addToFolder(List<File> files, IFolder targetFolder, IProgressMonitor monitor) throws CoreException, IOException {
		for (File fileToAdd : files) {
			monitor.subTask("Adding " + fileToAdd.toString() + " to folder: " + targetFolder.getName());
			File targetFile = new File(targetFolder.getRawLocation().toString(), fileToAdd.getName());
			targetFile.createNewFile();
			FileInputStream fis = new FileInputStream(fileToAdd);
			FileOutputStream fos = new FileOutputStream(targetFile);
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
			fis.close();
			fos.close();
			monitor.worked(1);
		}
	}

	/**
	 * This method retrieves an eclipse project from its name.
	 * 
	 * @param projectName
	 * @return
	 */
	public static IProject getProject(String projectName) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = null;
		if (projectName != null) {
			project = workspaceRoot.getProject(projectName);
		}
		return project;
	}

	public static String suffixWithCustomSuffix(String absolutePath, String customSuffix) {
		int lastDotIndex = absolutePath.lastIndexOf(".");
		String result = absolutePath;
		if (lastDotIndex == 0) {
			result = absolutePath + customSuffix;
		} else {
			result = absolutePath.substring(0, lastDotIndex) + customSuffix + absolutePath.substring(lastDotIndex, absolutePath.length());
		}
		if (new File(result).exists()) {
			result = suffixWithNumber(result);
		}
		return result;
	}

	public static String suffixWithNumber(String absolutePath) {
		int i = 1;
		int lastDotIndex = absolutePath.lastIndexOf(".");
		String result = absolutePath;
		if (lastDotIndex == 0) {
			result = absolutePath + "." + (i++);
		} else {
			result = absolutePath.substring(0, lastDotIndex) + "." + (i++) + absolutePath.substring(lastDotIndex, absolutePath.length());
		}
		File file = new File(result);
		while (file.exists()) {
			lastDotIndex = absolutePath.lastIndexOf(".");
			if (lastDotIndex == 0) {
				result = absolutePath + "." + (i++);
			} else {
				result = absolutePath.substring(0, lastDotIndex) + "." + (i++) + absolutePath.substring(lastDotIndex, absolutePath.length());
			}
			file = new File(result);
		}
		return result;
	}

	public static String removeStartingSeparator(String portableString) {
		String result = portableString;
		if (portableString.startsWith("\\")) {
			result = portableString.replaceFirst("\\", "");
		}
		if (portableString.startsWith("/")) {
			result = portableString.replaceFirst("/", "");
		}
		return result;
	}

	public static String checkForMissingEndingSeparator(String portableString) {
		String result = portableString;
		if (!(portableString.endsWith("\\") || portableString.endsWith("/")) && !portableString.equals("")) {
			result = portableString + "/";
		}
		return result;
	}

}