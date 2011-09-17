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
package org.org.eclipse.cheatsheet.commands.internal.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.cheatsheet.commands.handlers.ModeParameterValues;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.tools.ArchivesToolBox;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;

public class CreateProjectInWorkspaceJob extends Job {

	private static String JOB_ID = "CheatSheet helper: create project in workspace";
	private URL fileUrl;
	private String pathInArchive;
	private String targetProjectName;
	private String mode;
	private String customSuffix;

	public CreateProjectInWorkspaceJob(URL fileUrl, String pathInArchive, String targetProjectName, String mode, String customSuffix) {
		super(JOB_ID);
		this.fileUrl = fileUrl;
		this.pathInArchive = pathInArchive;
		this.targetProjectName = targetProjectName;
		this.mode = mode;
		this.customSuffix = customSuffix;
		this.setPriority(Job.LONG);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(CheatSheetJobs.JOB_FAMILY));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "Project created successfully.");
		try {
			if (!projectExistsInWorkspace(targetProjectName)) {
				decompressRemoteProjectArchiveIntoWorkspace(fileUrl, pathInArchive, targetProjectName, monitor);
			} else {
				if (mode.equals(ModeParameterValues.REPLACE)) {
					decompressRemoteProjectArchiveIntoWorkspace(fileUrl, pathInArchive, targetProjectName, monitor);
				} else if (mode.equals(ModeParameterValues.SUFFIX)) {
					String projectName = targetProjectName;
					if (customSuffix != null) {
						projectName = suffixWithCustomSuffix(targetProjectName);
					} else {
						projectName = suffixWithNumber(targetProjectName);
					}
					decompressRemoteProjectArchiveIntoWorkspace(fileUrl, pathInArchive, projectName, monitor);
				} else if (mode.equals(ModeParameterValues.SKIP)) {
					// Skip ;)
				}
			}
			monitor.worked(100);
		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while creating project:\n" + e.getMessage());
		} finally {
			monitor.done();
		}
		return result;
	}

	private String suffixWithCustomSuffix(String projectName) {
		String result = projectName + customSuffix;
		if (new File(result).exists()) {
			result = suffixWithNumber(result);
		}
		return result;
	}

	private String suffixWithNumber(String projectName) {
		int i = 1;
		String result = projectName + "(" + (i++) + ")";
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = workspaceRoot.findMember(result);
		while (member != null && member.getAdapter(IProject.class) != null) {
			result = projectName + "(" + (i++) + ")";
			member = workspaceRoot.findMember(result);
		}
		return result;
	}

	private void decompressRemoteProjectArchiveIntoWorkspace(URL fileUrl, String pathInArchive, String targetProjectName, IProgressMonitor monitor) throws IOException, CoreException {
		monitor.beginTask("Importing...", 1000);
		monitor.worked(100);
		File tmpFile = File.createTempFile("cheatsheets", ".zipfile");
		downloadToLocalFile(tmpFile, fileUrl, IOToolBox.determineProxy(fileUrl), monitor);
		IProject project = createProjectShellInWorkspace(targetProjectName, monitor);
		File targetFolder = project.getLocation().makeAbsolute().toFile();
		decompressArchive(tmpFile, targetFolder, pathInArchive, monitor);
		project.open(new SubProgressMonitor(monitor, 100));
		project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 100));
		// FIXME when possible, as this could be broken by future releases...
		// next step is necessary in order to have an up-to-date project name in the project's description (.project file)
		IProjectDescription projectDescription = project.getDescription();
		projectDescription.setName("");// project description has to have a difference from current one, but name will be ignored and replaced by project's FS name.
		project.setDescription(projectDescription, new SubProgressMonitor(monitor, 100));
	}

	private static void decompressArchive(File tmpFile, File targetFolder, String pathInArchive, IProgressMonitor monitor) throws IOException {
		monitor.subTask("Deflating archive.");
		ArchivesToolBox.decompressArchiveSubPartTo(tmpFile, targetFolder, pathInArchive);
		monitor.worked(100);
	}

	private static IProject createProjectShellInWorkspace(String targetProjectName, IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Creating project in workspace");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(targetProjectName);
		if (!project.exists()) {
			project.create(new SubProgressMonitor(monitor, 100));
		}
		if (project.isOpen()) {
			project.close(new SubProgressMonitor(monitor, 100));
		}
		monitor.worked(200);
		return project;
	}

	private boolean projectExistsInWorkspace(String targetProjectName) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = workspaceRoot.findMember(targetProjectName);
		return (member != null && member.getAdapter(IProject.class) != null);
	}

	private static void downloadToLocalFile(File targetFile, URL requestedURL, Proxy proxy, IProgressMonitor monitor) throws IOException {
		monitor.subTask("Downloading remote archived project to: " + targetFile.getAbsolutePath());
		InputStream is = null;
		FileOutputStream out = null;
		try {
			is = open(requestedURL, proxy);
			targetFile.createNewFile();
			out = new FileOutputStream(targetFile);
			byte[] buf = new byte[1024]; // 1K buffer
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
			monitor.worked(100);
		} finally {
			close(is);
			close(out);
		}
	}

	private static void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e1) {
				// ignore.
			}
		}
	}

	private static void close(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e1) {
				// ignore.
			}
		}
	}

	private static InputStream open(URL url, Proxy proxy) throws IOException {
		InputStream inputStream = null;
		if (proxy != null) {
			inputStream = url.openConnection(proxy).getInputStream();
		} else {
			inputStream = url.openConnection().getInputStream();
		}
		return inputStream;
	}

}
