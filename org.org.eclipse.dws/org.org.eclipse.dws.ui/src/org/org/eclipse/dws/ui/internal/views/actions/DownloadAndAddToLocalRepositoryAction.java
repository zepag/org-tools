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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.dws.core.internal.jobs.DownloadToLocalRepositoryJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.GroupsHolder;


/**
 * The Class DownloadAndAddToLocalRepositoryAction.
 * 
 * @author pagregoire
 */
public class DownloadAndAddToLocalRepositoryAction extends AbstractDWSViewAction {

	/**
	 * The Class ArtifactsRetrievalVisitor.
	 */
	private static class ArtifactsRetrievalVisitor implements IModelItemVisitor {

		/** The artifact versions. */
		private Set<ArtifactVersion> artifactVersions= new HashSet<ArtifactVersion>();

		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			boolean result = false;
			if (modelItem instanceof Group || modelItem instanceof GroupsHolder || modelItem instanceof Artifact) {
				result = true;
			} else if (modelItem instanceof ArtifactVersion) {
				artifactVersions.add((ArtifactVersion) modelItem);
			}
			return result;
		}

		/**
		 * Gets the artifact versions.
		 * 
		 * @return the artifact versions
		 */
		public Set<ArtifactVersion> getArtifactVersions() {
			return artifactVersions;
		}

	}

	/**
	 * Instantiates a new download and add to local repository action.
	 * 
	 * @param actionHost the action host
	 */
	public DownloadAndAddToLocalRepositoryAction(IActionHost actionHost) {
		super(actionHost);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object next = it.next();
			if (next instanceof IModelItem) {
				final IModelItem modelItem=(IModelItem)next;
				ArtifactsRetrievalVisitor artifactsRetrievalVisitor = new ArtifactsRetrievalVisitor();
				modelItem.accept(artifactsRetrievalVisitor);
				DownloadToLocalRepositoryJob downloadToLocalRepositoryJob = new DownloadToLocalRepositoryJob(modelItem,artifactsRetrievalVisitor.getArtifactVersions());
				downloadToLocalRepositoryJob.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification","Synchronization of \"" + modelItem.getUID() + "\" with local repository ended: \n"));
				downloadToLocalRepositoryJob.schedule();
			}
		}

	}
}