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
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.cheatsheet.commands.handlers.ModeParameterValues;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.tools.FileToolBox;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;

public class CreateFileInWorkspaceJob extends Job {

	private static String JOB_ID = "CheatSheet helper: create file in workspace";
	private final IProject project;
	private final URL fileUrl;
	private String targetFolder;
	private String targetFileName;
	private String mode;
	private String customSuffix;

	private IFile workbenchFile = null;

	public CreateFileInWorkspaceJob(IProject project, URL fileUrl, String targetFolder, String targetFileName, String mode, String customSuffix) {
		super(JOB_ID);
		this.project = project;
		this.fileUrl = fileUrl;
		this.targetFolder = targetFolder;
		this.targetFileName = targetFileName;
		this.mode = mode;
		this.customSuffix = customSuffix;
		this.setPriority(Job.SHORT);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(CheatSheetJobs.JOB_FAMILY));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "File created successfully.");
		try {
			File file = createFile(project, monitor);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IPath filePath = new Path(file.getAbsolutePath());
			workbenchFile = workspace.getRoot().getFileForLocation(filePath);
		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while creating file:\n" + e.getMessage());
		}
		monitor.done();
		return result;
	}

	private File createFile(IProject project, IProgressMonitor monitor) throws CoreException, IOException {
		String projectPath = FileToolBox.checkForMissingEndingSeparator(project.getLocation().makeAbsolute().toPortableString());
		targetFolder = FileToolBox.removeStartingSeparator(FileToolBox.checkForMissingEndingSeparator(targetFolder));
		targetFileName = FileToolBox.removeStartingSeparator(targetFileName);
		String filePath = projectPath + targetFolder + targetFileName;
		File file = doCreationDependingOnMode(project, fileUrl, filePath, monitor);
		return file;
	}

	private File doCreationDependingOnMode(IProject project, URL remoteFileUrl, String filePath, IProgressMonitor monitor) throws CoreException, IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			// no need to check the mode if the file exists.
			if (!file.getParentFile().exists()) {
				File parentFile = file.getParentFile();
				parentFile.mkdir();
			}
			IOToolBox.downloadToLocalFile(file, remoteFileUrl, IOToolBox.determineProxy(fileUrl), new SubProgressMonitor(monitor, 1));
		} else {
			if (!mode.equals(ModeParameterValues.SKIP)) {
				if (mode.equals(ModeParameterValues.REPLACE)) {
					IOToolBox.downloadToLocalFile(file, remoteFileUrl, IOToolBox.determineProxy(fileUrl), new SubProgressMonitor(monitor, 1));
				}
				if (mode.equals(ModeParameterValues.SUFFIX)) {
					String fileName = file.getAbsolutePath();
					if (customSuffix != null) {
						file = new File(FileToolBox.suffixWithCustomSuffix(fileName, customSuffix));
					} else {
						file = new File(FileToolBox.suffixWithNumber(fileName));
					}
					IOToolBox.downloadToLocalFile(file, remoteFileUrl, IOToolBox.determineProxy(fileUrl), new SubProgressMonitor(monitor, 1));
				}
			}
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
		return file;
	}

	public IFile getCreatedFile() {
		return workbenchFile;
	}

}