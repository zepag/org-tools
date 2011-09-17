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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.eclipse.dws.core.internal.jobs.RefreshArtifactJob;
import org.org.eclipse.dws.core.internal.jobs.RefreshGroupJob;
import org.org.eclipse.dws.core.internal.jobs.RefreshRepositoryJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.Group;


/**
 * The Class RefreshItemAction.
 * 
 * @author pagregoire
 */
public class RefreshItemAction extends AbstractSimpleAction {

	/** The resolver. */
	private IActionResolver resolver;

	/** The action host. */
	private IActionHost actionHost;

	/**
	 * Instantiates a new refresh item action.
	 * 
	 * @param actionHost the action host
	 */
	public RefreshItemAction(IActionHost actionHost) {
		this.actionHost = actionHost;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		boolean result = false;
		if (resolver == null) {
			result = false;
		} else {
			result = resolver.isEnabled();
		}
		return result;
	}

	/**
	 * Sets the resolver.
	 * 
	 * @param resolver the new resolver
	 */
	public void setResolver(IActionResolver resolver) {
		this.resolver = resolver;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object next = it.next();
			if (next instanceof CrawledRepository) {
				final CrawledRepository crawledRepository = (CrawledRepository) next;
				Job job = new RefreshRepositoryJob(crawledRepository);
				job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", "CrawledRepository \"" + crawledRepository.getLabel()+ "\" refresh ended: \n" ));
				job.schedule();
			}
			if (next instanceof Group) {
				final Group group = (Group) next;
				Job job = new RefreshGroupJob(group);
				job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", "Group \"" + group.getName()+ "\" refresh ended: \n" ));
				job.schedule();
			}
			if (next instanceof Artifact) {
				final Artifact artifact = (Artifact) next;
				Job job = new RefreshArtifactJob(artifact);
				job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", "Group \"" + artifact.getId()+ "\" refresh ended: \n" ));
				job.schedule();
			}
		}
	}

}
