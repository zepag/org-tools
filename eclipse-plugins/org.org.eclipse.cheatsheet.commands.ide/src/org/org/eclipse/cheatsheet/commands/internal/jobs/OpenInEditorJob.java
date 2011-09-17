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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.org.eclipse.core.utils.platform.jobs.BatchSimilarRule;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;

public class OpenInEditorJob extends Job {

	private static String JOB_ID = "CheatSheet helper: open file in editor";
	private IWorkbenchPage workbenchPage;
	private Integer lineNumber;
	private String filePath;

	public OpenInEditorJob(IWorkbenchPage workbenchPage, Integer lineNumber, String filePath) {
		super(JOB_ID);
		this.workbenchPage = workbenchPage;
		this.lineNumber = (lineNumber == null || lineNumber <= 1) ? 1 : lineNumber;
		this.filePath = filePath;
		this.setPriority(Job.SHORT);
		this.setUser(true);
		this.setRule(new BatchSimilarRule(CheatSheetJobs.JOB_FAMILY));
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = new StatusInfo(IStatus.OK, "File successfully opened.");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath filePath = new Path(this.filePath);
		final IFile file = workspace.getRoot().getFile(filePath);
		if (file != null && file.exists()) {
			Display.getDefault().asyncExec(new Runnable() {

				@SuppressWarnings("unchecked")
				public void run() {
					try {
						Map map = new HashMap();
						map.put(IMarker.LINE_NUMBER, lineNumber);
						IMarker marker = file.createMarker(IMarker.TEXT);
						marker.setAttributes(map);
						IDE.openEditor(workbenchPage, marker);
					} catch (PartInitException e) {
						e.printStackTrace();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}

			});

		}else{
			result = new StatusInfo(IStatus.ERROR, "File does not appear to exist.");
		}
		monitor.done();
		return result;
	}
}