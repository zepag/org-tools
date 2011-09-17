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
package org.org.eclipse.dws.ui.internal.views.actions;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.dws.core.internal.jobs.DownloadToLocalFileSystemJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;
import org.org.model.IModelItem;

/**
 * The Class DownloadAndAddToLocalRepositoryAction.
 * 
 * @author pagregoire
 */
public class DownloadToLocalFileSystemAction extends AbstractDWSViewAction {

	/**
	 * Instantiates a new download and add to local repository action.
	 * 
	 * @param actionHost
	 *            the action host
	 */
	public DownloadToLocalFileSystemAction(IActionHost actionHost) {
		super(actionHost);
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		DirectoryDialog directoryDialog = new DirectoryDialog(getActionHost().getShell());
		String targetPath = directoryDialog.open();
		if (targetPath != null) {
			IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object next = it.next();
				if (next instanceof IModelItem) {
					final IModelItem modelItem = (IModelItem) next;
					ArtifactsRetrievalVisitor artifactsRetrievalVisitor = new ArtifactsRetrievalVisitor();
					modelItem.accept(artifactsRetrievalVisitor);
					DownloadToLocalFileSystemJob downloadToLocalFileSystemJob = new DownloadToLocalFileSystemJob(modelItem, artifactsRetrievalVisitor.getArtifactVersions(), targetPath);
					downloadToLocalFileSystemJob.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", "Download of \"" + modelItem.getUID() + "\" to local file system ended: \n"));
					downloadToLocalFileSystemJob.schedule();
				}
			}
		}
	}
}