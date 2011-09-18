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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.ui.internal.views.MavenRepositoriesView;
import org.org.model.IModelItemListener;
import org.org.model.ModelItemEvent;
import org.org.model.RootModelItem;
import org.org.repository.crawler.IExternalInterruptionFlagSetter;
import org.org.repository.crawler.InterruptionFlag;
import org.org.repository.crawler.maven2.model.CrawledRepository;


/**
 * The Class RemoveRepositoryAction.
 * 
 * @author pagregoire
 */
public class RemoveRepositoryAction extends AbstractSimpleAction {

	/** The resolver. */
	private IActionResolver resolver;

	/** The action host. */
	private IActionHost actionHost;

	/**
	 * Instantiates a new removes the repository action.
	 * 
	 * @param actionHost the action host
	 */
	public RemoveRepositoryAction(IActionHost actionHost) {
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
	@SuppressWarnings("rawtypes")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object next = it.next();
			if (next instanceof CrawledRepository) {
				CrawledRepository crawledRepository = (CrawledRepository) next;
				RootModelItem.<CrawledRepository>getInstance(ModelConstants.REPOSITORIES_ROOT).removeChild(crawledRepository.getUID());
			}
		}
		Job job = new Job("Refreshing Model") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(getName(), 1000);
				final IProgressMonitor fMonitor = monitor;
				IModelItemListener modelItemListener = new IModelItemListener() {
					public void changeOccured(ModelItemEvent modelItemEvent) {
						if (modelItemEvent.getEventType() == ModelItemEvent.EventType.PRE_REMOVE_CHILD) {
							fMonitor.subTask("Removing " + modelItemEvent.getTargetItem().getUID() + " from " + modelItemEvent.getSourceItem().getUID());
							fMonitor.worked(1);
							MavenRepositoriesView.refreshViewer();
						}
					}
				};

				IExternalInterruptionFlagSetter externalInterruptionFlagSetter = new IExternalInterruptionFlagSetter() {

					public InterruptionFlag processStatus() {
						InterruptionFlag flag = new InterruptionFlag();
						if (fMonitor.isCanceled()) {
							flag.setCurrentStatus(InterruptionFlag.STOP);
						}
						return flag;
					}

				};
				if (RepositoryModelPersistence.getWorkspacePersistencesStatus().equals(RepositoryModelPersistence.OUT_OF_SYNC)) {
//					RootModelItem.getInstance(ModelConstants.REPOSITORIES_ROOT).addListener(modelItemListener);
					RepositoryModelPersistence.refreshModel(externalInterruptionFlagSetter,modelItemListener);
//					RootModelItem.getInstance(ModelConstants.REPOSITORIES_ROOT).removeListener(modelItemListener);
				}
				monitor.done();
				return new StatusInfo(IStatus.OK, "refreshing done");
			}
		};
		job.setPriority(Job.LONG);
		job.setSystem(true);
		job.schedule();
		RepositoryModelPersistence.saveRepositoryInfo();
		MavenRepositoriesView.refreshViewer();
	}
}