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
package org.org.eclipse.cheatsheet.catalog.internal.views;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.org.eclipse.cheatsheet.catalog.CheatSheetCatalogPlugin;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelConstants;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelPersistence;
import org.org.model.IModelItem;
import org.org.model.IModelItemListener;
import org.org.model.ModelItemEvent;
import org.org.model.RootModelItem;

class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	private final class ProgressModelListener implements IModelItemListener {
		private final IProgressMonitor monitor;
		private final TreeViewer viewer;

		private ProgressModelListener(IProgressMonitor monitor, TreeViewer viewer) {
			this.monitor = monitor;
			this.viewer = viewer;
		}

		public void changeOccured(ModelItemEvent modelItemEvent) {
			if (modelItemEvent.getEventType() == ModelItemEvent.EventType.PRE_ADD_CHILD) {
				monitor.subTask("Adding " + modelItemEvent.getTargetItem().getUID() + " to " + modelItemEvent.getSourceItem().getUID());
				monitor.worked(1);
				refreshViewer();
			}
		}

		private void refreshViewer() {
			ModelPersistence.checkStatus();
			try {
				viewer.refresh(true);
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}

	/**
	 * 
	 */
	private final CheatSheetCatalogView cheatSheetCatalogView;
	private RootModelItem<CheatSheetCatalog> invisibleRoot;
	private TreeViewer viewer;

	public ViewContentProvider(CheatSheetCatalogView cheatSheetCatalogView, TreeViewer treeViewer) {
		this.cheatSheetCatalogView = cheatSheetCatalogView;
		this.viewer = treeViewer;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		if (parent.equals(this.cheatSheetCatalogView.getViewSite())) {
			if (invisibleRoot == null)
				initialize();
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	@SuppressWarnings("rawtypes")
	public Object getParent(Object child) {
		if (child instanceof IModelItem) {
			return ((IModelItem) child).getParent();
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object[] getChildren(Object parent) {
		if (parent instanceof IModelItem) {
			Set<IModelItem> children = ((IModelItem) parent).getChildren();
			return children.toArray(new IModelItem[children.size()]);
		}
		return new Object[0];
	}

	@SuppressWarnings("rawtypes")
	public boolean hasChildren(Object parent) {
		if (parent instanceof IModelItem)
			return ((IModelItem) parent).hasChildren();
		return false;
	}

	/*
	 * We will set up a dummy model to initialize tree heararchy. In a real code, you will connect to a real model and expose its hierarchy.
	 */
	private void initialize() {
		ModelPersistence.loadCheatSheetCatalogs();
		invisibleRoot = RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID);
		ModelPersistence.saveCheatSheetCatalogs();
		Job job = new Job("Refreshing Model") {

			protected IStatus run(final IProgressMonitor fMonitor) {
				fMonitor.beginTask(getName(), 1000);
				IModelItemListener modelItemListener = new ProgressModelListener(fMonitor, viewer);
				if (ModelPersistence.getWorkspacePersistencesStatus().equals(ModelPersistence.OUT_OF_SYNC)) {
					ModelPersistence.refreshModel(modelItemListener);
				}
				fMonitor.done();
				return new Status(IStatus.INFO, CheatSheetCatalogPlugin.PLUGIN_ID, "refreshing done");
			}

		};
		job.setPriority(Job.LONG);
	}
}