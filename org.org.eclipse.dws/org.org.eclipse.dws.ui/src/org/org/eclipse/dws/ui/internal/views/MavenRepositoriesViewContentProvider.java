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
package org.org.eclipse.dws.ui.internal.views;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.model.IModelItem;
import org.org.model.IModelItemListener;
import org.org.model.ModelItemEvent;
import org.org.model.RootModelItem;
import org.org.repository.crawler.IExternalInterruptionFlagSetter;
import org.org.repository.crawler.InterruptionFlag;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * The Class MavenRepositoriesViewContentProvider.
 */
class MavenRepositoriesViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	/** The invisible root. */
	private RootModelItem<CrawledRepository> invisibleRoot;

	/** The view site. */
	private IViewSite viewSite;

	/** The listener. */
	private IModelItemListener listener;

	/**
	 * Instantiates a new maven repositories view content provider.
	 * 
	 * @param viewSite
	 *            the view site
	 */
	public MavenRepositoriesViewContentProvider(IViewSite viewSite) {
		this.viewSite = viewSite;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		invisibleRoot.removeListener(listener);
		invisibleRoot = null;
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		// if parent is the viewSite, get the root element
		if (parent.equals(this.viewSite)) {
			if (invisibleRoot == null) {
				initialize();
			}
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		if (!RootModelItem.isInstanciated(ModelConstants.REPOSITORIES_ROOT)) {
			RepositoryModelPersistence.loadRepositoryInfo();
		}
		invisibleRoot = RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT);
		RepositoryModelPersistence.saveRepositoryInfo();
		Job job = new Job("Refreshing Model") {
			@Override
			protected IStatus run(final IProgressMonitor fMonitor) {
				fMonitor.beginTask(getName(), 1000);
				final Object[] objects = new Object[] { fMonitor };
				IModelItemListener modelItemListener = new IModelItemListener() {
					public void changeOccured(ModelItemEvent modelItemEvent) {
						if (modelItemEvent.getEventType() == ModelItemEvent.EventType.PRE_ADD_CHILD) {
							((IProgressMonitor) objects[0]).subTask("Adding " + modelItemEvent.getTargetItem().getUID() + " to " + modelItemEvent.getSourceItem().getUID());
							((IProgressMonitor) objects[0]).worked(1);
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
					// RootModelItem.getInstance(ModelConstants.REPOSITORIES_ROOT).addListener(modelItemListener);
					RepositoryModelPersistence.refreshModel(externalInterruptionFlagSetter, modelItemListener);
					// RootModelItem.getInstance(ModelConstants.REPOSITORIES_ROOT).removeListener(modelItemListener);
				}
				fMonitor.done();
				return new StatusInfo(IStatus.OK, "refreshing done");
			}
		};
		job.setPriority(Job.LONG);
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	public Object getParent(Object child) {
		if (child instanceof IModelItem) {
			return ((IModelItem) child).getParent();
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object[] getChildren(Object parent) {
		if (parent instanceof IModelItem) {
			Set<IModelItem> children = ((IModelItem) parent).getChildren();
			return children.toArray(new IModelItem[children.size()]);
		}
		return new Object[0];
	}

	/**
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@SuppressWarnings({ "rawtypes" })
	public boolean hasChildren(Object parent) {
		if (parent instanceof IModelItem)
			return ((IModelItem) parent).hasChildren();
		return false;
	}
}