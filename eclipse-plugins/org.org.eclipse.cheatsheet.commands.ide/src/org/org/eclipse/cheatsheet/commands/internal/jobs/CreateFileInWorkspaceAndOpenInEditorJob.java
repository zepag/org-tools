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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

public class CreateFileInWorkspaceAndOpenInEditorJob extends CreateFileInWorkspaceJob {

	private IWorkbenchPage workbenchPage;
	private Integer lineNumber;

	public CreateFileInWorkspaceAndOpenInEditorJob(IWorkbenchPage workbenchPage, Integer lineNumber, IProject project, URL fileUrl, String targetFolder, String targetFileName, String mode, String customSuffix) {
		super(project, fileUrl, targetFolder, targetFileName, mode, customSuffix);
		this.workbenchPage = workbenchPage;
		this.lineNumber = (lineNumber == null || lineNumber <= 1) ? 1 : lineNumber;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IStatus result = super.run(monitor);
		final IFile file = super.getCreatedFile();
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

		}
		monitor.done();
		return result;
	}
}