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
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
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
import org.org.eclipse.core.utils.platform.tools.ArchivesToolBox;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.core.utils.platform.tools.ArchivesToolBox.WriteMode;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;

public class ExtractContentsInProjectJob extends Job {

	private static String JOB_ID = "CheatSheet helper: extract contents in project";

	private URL fileUrl;
	private String pathInArchive;
	private String pathInProject;
	private String targetProjectName;
	private String mode;
	private String customSuffix;

	public ExtractContentsInProjectJob(String targetProjectName, URL fileUrl, String pathInArchive, String pathInProject, String mode, String customSuffix) {
		super(JOB_ID);
		this.targetProjectName = targetProjectName;
		this.fileUrl = fileUrl;
		this.pathInArchive = pathInArchive;
		this.pathInProject = pathInProject;
		this.mode = mode;
		this.customSuffix = customSuffix;
	}

	public IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "Content extracted successfully.");
		try {
			if (!projectExistsInWorkspace(targetProjectName)) {
				decompressRemoteProjectArchiveIntoWorkspace(fileUrl, pathInArchive, targetProjectName, pathInProject, mode, monitor);
			} else {
				if (mode.equals(ModeParameterValues.REPLACE)) {
					decompressRemoteProjectArchiveIntoWorkspace(fileUrl, pathInArchive, targetProjectName, pathInProject, mode, monitor);
				} else if (mode.equals(ModeParameterValues.SUFFIX)) {
					decompressRemoteProjectArchiveIntoWorkspace(fileUrl, pathInArchive, targetProjectName, pathInProject, mode, monitor);
				} else if (mode.equals(ModeParameterValues.SKIP)) {
					// Skip ;)
				}
			}
		} catch (Throwable e) {
			monitor.setCanceled(true);
			result = new StatusInfo(IStatus.ERROR, "A problem occured while extracting data:\n" + e.getMessage());
		}
		monitor.done();
		return result;
	}

	private void decompressRemoteProjectArchiveIntoWorkspace(URL fileUrl, String pathInArchive, String targetProjectName, String pathInProject, final String mode, IProgressMonitor createProgressGroup) throws IOException, CoreException, URISyntaxException {
		File tmpFile = File.createTempFile("cheatsheets", ".zipfile");
		downloadToLocalFile(tmpFile, fileUrl, IOToolBox.determineProxy(fileUrl));
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(targetProjectName);
		File targetFolder = project.getLocation().makeAbsolute().toFile();
		targetFolder = new File(targetFolder, pathInProject);
		targetFolder.mkdirs();
		ArchivesToolBox.decompressArchiveSubPartTo(tmpFile, targetFolder, pathInArchive, new ArchivesToolBox.IWriteHinter() {

			public WriteMode getFileWriteMode(File file) {
				WriteMode result = WriteMode.REPLACE;
				if (file.exists()) {
					if (mode.equals(ModeParameterValues.SKIP)) {
						result = WriteMode.SKIP;
					}
				}
				return result;
			}

			public String alterFolderName(File targetFolder, String folderName) {
				return folderName;
			}

			public String alterFileName(File targetFolder, String fileName) {
				String result = fileName;
				File file = new File(targetFolder, fileName);
				if (file.exists()) {
					if (mode.equals(ModeParameterValues.SUFFIX)) {
						if (customSuffix != null) {
							result = suffixFileNameWithCustomSuffix(targetFolder, fileName);
						} else {
							result = suffixFileNameWithNumber(targetFolder, fileName);
						}
					}
				}
				return result;
			}

			private String suffixFileNameWithNumber(File targetFolder, String fileName) {
				int i = 1;
				int lastDotIndex = fileName.lastIndexOf(".");
				String result = fileName;
				if (lastDotIndex == 0) {
					result = fileName + "." + (i++);
				} else {
					result = fileName.substring(0, lastDotIndex) + "." + (i++) + fileName.substring(lastDotIndex, fileName.length());
				}
				File file = new File(targetFolder, result);
				while (file.exists()) {
					lastDotIndex = fileName.lastIndexOf(".");
					if (lastDotIndex == 0) {
						result = fileName + "." + (i++);
					} else {
						result = fileName.substring(0, lastDotIndex) + "." + (i++) + fileName.substring(lastDotIndex, fileName.length());
					}
					file = new File(targetFolder, result);
				}
				return result;
			}

			private String suffixFileNameWithCustomSuffix(File targetFolder, String fileName) {
				String result = fileName;
				int lastDotIndex = fileName.lastIndexOf(".");
				if (lastDotIndex == 0) {
					result = fileName + customSuffix;
				} else {
					result = fileName.substring(0, lastDotIndex) + customSuffix + fileName.substring(lastDotIndex, fileName.length());
				}
				if (new File(targetFolder, result).exists()) {
					result = suffixFileNameWithNumber(targetFolder, result);
				}
				return result;
			}

		});
		if (!project.isOpen()) {
			project.open(new SubProgressMonitor(createProgressGroup, 1));
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(createProgressGroup, 1));
	}

	private boolean projectExistsInWorkspace(String targetProjectName) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = workspaceRoot.findMember(targetProjectName);
		return (member != null && member.getAdapter(IProject.class) != null);
	}

	private static void downloadToLocalFile(File targetFile, URL requestedURL, Proxy proxy) {
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
		} catch (IOException e) {
			e.printStackTrace();
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
