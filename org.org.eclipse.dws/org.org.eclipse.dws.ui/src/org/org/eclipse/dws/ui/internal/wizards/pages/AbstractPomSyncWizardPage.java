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
package org.org.eclipse.dws.ui.internal.wizards.pages;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.ui.dialogs.WarningDialog;
import org.org.eclipse.core.utils.platform.fields.ITreeListAdapter;
import org.org.eclipse.core.utils.platform.fields.ListDialogField;
import org.org.eclipse.core.utils.platform.fields.SelectionButtonDialogField;
import org.org.eclipse.core.utils.platform.fields.TreeListDialogField;
import org.org.eclipse.core.utils.platform.tools.FileToolBox;
import org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage;
import org.org.eclipse.core.utils.platform.wizards.page.WizardContentsHelper;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.configuration.properties.PropertiesFacade;
import org.org.eclipse.dws.core.internal.configuration.properties.PropertiesNames;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;
import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.ResolvedArtifact;
import org.org.eclipse.dws.core.internal.model.SkippedDependency;
import org.org.eclipse.dws.core.internal.model.UnresolvedArtifact;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.eclipse.dws.ui.internal.handlers.PomJavaSynchronizationHandler;
import org.org.eclipse.dws.ui.internal.images.PluginImages;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.GroupsHolder;

/**
 * The Class AbstractPomSyncWizardPage.
 */
public abstract class AbstractPomSyncWizardPage extends AbstractWizardCustomPage {

	/**
	 * The Class LibrariesLabelProvider gives adequate labels to items displayed in the TreeViews of the wizards.
	 */
	public static class LibrariesLabelProvider extends LabelProvider {

		/** The page, passed as a reference. */
		private AbstractPomSyncWizardPage page;

		/**
		 * Instantiates a new libraries label provider.
		 * 
		 * @param page
		 *            the page
		 */
		public LibrariesLabelProvider(AbstractPomSyncWizardPage page) {
			this.page = page;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		/**
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object obj) {
			String result = null;
			if (obj instanceof AbstractChosenArtifactVersion) {
				AbstractChosenArtifactVersion artifactVersionListElement = (AbstractChosenArtifactVersion) obj;
				if (artifactVersionListElement instanceof ResolvedArtifact) {
					ArtifactVersion artifactVersion = ((ResolvedArtifact) artifactVersionListElement).getArtifactVersion();
					String scope = artifactVersionListElement.getScope().name().toLowerCase();
					if (isScopeUnknown((ResolvedArtifact) artifactVersionListElement)) {
						scope = WizardsMessages.AbstractPomSyncWizardPage_unknown_scope + scope;
					}
					GroupsHolder groupsHolder = artifactVersion.getParent().getParent().getParent();
					String url = null;
					if (groupsHolder instanceof CrawledRepository) {
						CrawledRepository crawledRepository = (CrawledRepository) groupsHolder;
						url = artifactVersionListElement.getScope() == Scope.SYSTEM ? (artifactVersionListElement.getSystemPath() == null ? crawledRepository.getUID() : artifactVersionListElement.getSystemPath()) : crawledRepository.getUID();
					}
					if (groupsHolder instanceof LibraryPack) {
						LibraryPack libraryPack = (LibraryPack) groupsHolder;
						url = "Library Pack: " + libraryPack.getLabel();
					}
					result = MessageFormat.format(WizardsMessages.AbstractPomSyncWizardPage_resolvedArtifactDescription, new Object[] { artifactVersion.getId(), scope, url });
				} else if (artifactVersionListElement instanceof UnresolvedArtifact) {
					PomDependency pomDependency = ((UnresolvedArtifact) artifactVersionListElement).getUnresolvedPomDependency();
					result = MessageFormat.format(WizardsMessages.AbstractPomSyncWizardPage_unresolvedArtifactDescription, new Object[] { pomDependency.getArtifactId(), pomDependency.getVersion() + (pomDependency.getClassifier() == null ? "" : "-" + pomDependency.getClassifier()) });
				}
			} else if (obj instanceof DWSClasspathEntryDescriptor) {
				DWSClasspathEntryDescriptor classpathEntry = (DWSClasspathEntryDescriptor) obj;
				result = classpathEntry.getPath();
			} else {
				result = obj.toString();
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		/**
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		@Override
		public Image getImage(Object obj) {
			Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			if (obj instanceof AbstractChosenArtifactVersion) {
				AbstractChosenArtifactVersion artifactVersionListElement = (AbstractChosenArtifactVersion) obj;
				if (artifactVersionListElement instanceof ResolvedArtifact) {
					ResolvedArtifact resolvedArtifact = (ResolvedArtifact) artifactVersionListElement;
					if (isInvolvedInConflict(resolvedArtifact) || isScopeUnknown(resolvedArtifact)) {
						image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_CONFLICT_16);
					} else {
						image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16);
					}
				} else if (artifactVersionListElement instanceof UnresolvedArtifact) {
					image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_ERROR_16);
				}
			} else if (obj instanceof DWSClasspathEntryDescriptor) {
				image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_ERROR_16);
			} else if (obj instanceof PomDependency) {
				if (isInvolvedInConflict((PomDependency) obj)) {
					image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_CONFLICT_16);
				} else {
					image = DWSUIPlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16);
				}
			}
			return image;
		}

		/**
		 * Checks if is scope unknown.
		 * 
		 * @param resolvedArtifact
		 *            the resolved artifact
		 * 
		 * @return true, if is scope unknown
		 */
		private boolean isScopeUnknown(ResolvedArtifact resolvedArtifact) {
			boolean notAScope = false;
			if (resolvedArtifact.getScope() == null || resolvedArtifact.getScope() == Scope.OTHER) {
				notAScope = true;
			}
			return notAScope;
		}

		/**
		 * Checks if is involved in conflict.
		 * 
		 * @param resolvedArtifact
		 *            the resolved artifact
		 * 
		 * @return true, if is involved in conflict
		 */
		private boolean isInvolvedInConflict(ResolvedArtifact resolvedArtifact) {
			boolean result = false;
			String groupId = (resolvedArtifact.getArtifactVersion().getParent().getParent()).getName();
			String artifactId = resolvedArtifact.getArtifactVersion().getParent().getUID();
			result = page.ARTIFACTS_PONDERATION.get(groupId + "|" + artifactId) > 1; //$NON-NLS-1$
			return result;
		}

		/**
		 * Checks if is involved in conflict.
		 * 
		 * @param pomDependency
		 *            the pom dependency
		 * 
		 * @return true, if is involved in conflict
		 */
		private boolean isInvolvedInConflict(PomDependency pomDependency) {
			boolean result = false;
			String groupId = pomDependency.getGroupId();
			String artifactId = pomDependency.getArtifactId();
			result = page.ARTIFACTS_PONDERATION.get(groupId + "|" + artifactId) > 1; //$NON-NLS-1$
			return result;
		}
	}

	/**
	 * The Class LibrariesListAdapter allows to adapt a given model to the TreeList visual model.
	 */
	public static class LibrariesListAdapter implements ITreeListAdapter {

		/** The page. */
		private AbstractPomSyncWizardPage page;

		/**
		 * Instantiates a new libraries list adapter.
		 * 
		 * @param page
		 *            the page
		 */
		public LibrariesListAdapter(AbstractPomSyncWizardPage page) {
			this.page = page;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public boolean hasChildren(TreeListDialogField field, Object element) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public Object getParent(TreeListDialogField field, Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public Object[] getChildren(TreeListDialogField field, Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(TreeListDialogField treeListDialogField, KeyEvent e) {
			if (e.keyCode == 127 || e.keyCode == 8) {
				page.removeSelectedElements(treeListDialogField);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void doubleClicked(TreeListDialogField field) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void selectionChanged(TreeListDialogField field) {
			page.touch();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		public void customButtonPressed(TreeListDialogField field, int index) {
			// will never happen...
		}

	}

	/**
	 * The Class ConflictingListAdapter allows to adapt a given model to the TreeList visual model.
	 */
	public static class ConflictingListAdapter implements ITreeListAdapter {

		/** The page. */
		private AbstractPomSyncWizardPage page;

		/**
		 * Instantiates a new conflicting list adapter.
		 * 
		 * @param page
		 *            the page
		 */
		public ConflictingListAdapter(AbstractPomSyncWizardPage page) {
			this.page = page;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public boolean hasChildren(TreeListDialogField field, Object element) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public Object getParent(TreeListDialogField field, Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public Object[] getChildren(TreeListDialogField field, Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(TreeListDialogField treeListDialogField, KeyEvent e) {
			if (e.keyCode == 127 || e.keyCode == 8) {
				IClasspathEntry classpathEntry = (IClasspathEntry) treeListDialogField.getSelectedElements().get(0);
				if (classpathEntry != null) {
					treeListDialogField.removeElements(treeListDialogField.getSelectedElements());
					page.validateLibraries = true;
					page.refreshLibraryFields();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void doubleClicked(TreeListDialogField field) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void selectionChanged(TreeListDialogField field) {
			page.touch();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		public void customButtonPressed(TreeListDialogField field, int index) {
			// will never happen...
		}

	}

	/**
	 * The Class TransitiveListAdapter allows to adapt a given model to the TreeList visual model.
	 */
	public static class TransitiveListAdapter implements ITreeListAdapter {

		/** The page. */
		private AbstractPomSyncWizardPage page;

		/**
		 * Instantiates a new transitive list adapter.
		 * 
		 * @param page
		 *            the page
		 */
		public TransitiveListAdapter(AbstractPomSyncWizardPage page) {
			this.page = page;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		@SuppressWarnings("rawtypes")
		public boolean hasChildren(TreeListDialogField field, Object element) {
			return ((IModelItem) element).hasChildren();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		@SuppressWarnings("rawtypes")
		public Object getParent(TreeListDialogField field, Object element) {
			return ((IModelItem) element).getParent();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		@SuppressWarnings("rawtypes")
		public Object[] getChildren(TreeListDialogField field, Object element) {
			return ((IModelItem) element).getChildren().toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(TreeListDialogField treeListDialogField, KeyEvent e) {
			if (e.keyCode == 127 || e.keyCode == 8) {
				AbstractChosenArtifactVersion transitiveDepencency = (AbstractChosenArtifactVersion) treeListDialogField.getSelectedElements().get(0);
				if (transitiveDepencency != null) {
					treeListDialogField.removeElements(treeListDialogField.getSelectedElements());
					page.validateLibraries = true;
					if (transitiveDepencency instanceof ResolvedArtifact) {
						String groupId = (((ResolvedArtifact) transitiveDepencency).getArtifactVersion().getParent().getParent()).getName();
						String artifactId = (((ResolvedArtifact) transitiveDepencency).getArtifactVersion().getParent()).getId();
						String key = groupId + "|" + artifactId; //$NON-NLS-1$
						int ponderation = page.ARTIFACTS_PONDERATION.get(key) == null ? 0 : page.ARTIFACTS_PONDERATION.get(key);
						page.ARTIFACTS_PONDERATION.put(key, --ponderation);
					}
					page.refreshLibraryFields();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void doubleClicked(TreeListDialogField field) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void selectionChanged(TreeListDialogField field) {
			page.touch();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		public void customButtonPressed(TreeListDialogField field, int index) {
			// will never happen...
		}

	}

	/**
	 * The Class UndeterminedListAdapter allows to adapt a given model to the TreeList visual model.
	 */
	public class UndeterminedListAdapter implements ITreeListAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public boolean hasChildren(TreeListDialogField field, Object element) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public Object getParent(TreeListDialogField field, Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public Object[] getChildren(TreeListDialogField field, Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(TreeListDialogField field, KeyEvent e) {
			if (e.keyCode == 127 || e.keyCode == 8) {
				ResolvedArtifact resolvedArtifact = (ResolvedArtifact) field.getSelectedElements().get(0);
				if (resolvedArtifact != null) {
					field.removeElements(field.getSelectedElements());
					validateLibraries = true;
					String groupId = (resolvedArtifact.getArtifactVersion().getParent().getParent()).getName();
					String artifactId = resolvedArtifact.getArtifactVersion().getParent().getUID();
					String key = groupId + "|" + artifactId; //$NON-NLS-1$
					int ponderation = ARTIFACTS_PONDERATION.get(key) == null ? 0 : ARTIFACTS_PONDERATION.get(key);
					ARTIFACTS_PONDERATION.put(key, --ponderation);
					refreshLibraryFields();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void doubleClicked(TreeListDialogField field) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void selectionChanged(TreeListDialogField field) {
			touch();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		/**
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		public void customButtonPressed(TreeListDialogField field, int index) {
			// will never happen...
		}

	}

	/** The ARTIFACTS' ponderation. */
	protected final Map<String, Integer> ARTIFACTS_PONDERATION;

	/** The LIBRARIES. */
	protected final Set<AbstractChosenArtifactVersion> LIBRARIES;

	/** The result. */
	private CrawledRepository result = null;

	/** The validate libraries. */
	private Boolean validateLibraries = true;

	/** The CHOSENPROJECTNAME. */
	protected final String CHOSENPROJECTNAME;

	/** The PROJEC t_ names. */
	protected final String[] PROJECT_NAMES;

	/** The libraries ok. */
	private boolean librariesOk;

	/** The chosen project name. */
	private String chosenProjectName = null;

	// protected final Boolean CONSIDER_OPTIONAL;

	/** The issues libraries dialog field. */
	protected ListDialogField issuesLibrariesDialogField;

	/** The remove conflicting dialog field. */
	protected SelectionButtonDialogField removeConflictingDialogField;

	/** The conflicting libraries dialog field. */
	protected TreeListDialogField conflictingLibrariesDialogField;

	/** The add transitive dependencies field. */
	protected SelectionButtonDialogField addTransitiveDependenciesField;

	/** The transitive dependencies dialog field. */
	protected TreeListDialogField transitiveDependenciesDialogField;

	/**
	 * Gets the artifacts ponderation.
	 * 
	 * @param libraries
	 *            the libraries
	 * 
	 * @return the artifacts ponderation
	 */
	protected Map<String, Integer> getArtifactsPonderation(Set<AbstractChosenArtifactVersion> libraries) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (AbstractChosenArtifactVersion abstractChosenArtifactVersion : libraries) {
			if (abstractChosenArtifactVersion instanceof ResolvedArtifact) {
				ResolvedArtifact resolvedArtifact = (ResolvedArtifact) abstractChosenArtifactVersion;
				String groupId = (resolvedArtifact.getArtifactVersion().getParent().getParent()).getName();
				String artifactId = resolvedArtifact.getArtifactVersion().getParent().getUID();
				String key = groupId + "|" + artifactId; //$NON-NLS-1$
				int ponderation = result.get(key) == null ? 0 : result.get(key);
				result.put(key, ++ponderation);
				if (resolvedArtifact.hasTransitiveDependencies()) {
					getArtifactsPonderation(resolvedArtifact.getTransitiveDependencies(), result);
				}
			}
		}
		return result;
	}

	/**
	 * Gets the artifacts ponderation.
	 * 
	 * @param transitiveDependencies
	 *            the transitive dependencies
	 * @param result
	 *            the result
	 * 
	 * @return the artifacts ponderation
	 */
	private void getArtifactsPonderation(Set<AbstractChosenArtifactVersion> transitiveDependencies, Map<String, Integer> result) {
		for (AbstractChosenArtifactVersion transitiveDependency : transitiveDependencies) {
			if (transitiveDependency instanceof ResolvedArtifact) {
				ResolvedArtifact resolvedArtifact = (ResolvedArtifact) transitiveDependency;
				String groupId = (resolvedArtifact.getArtifactVersion().getParent().getParent()).getName();
				String artifactId = (resolvedArtifact.getArtifactVersion().getParent()).getId();
				String key = groupId + "|" + artifactId; //$NON-NLS-1$
				int ponderation = result.get(key) == null ? 0 : result.get(key);
				result.put(key, ++ponderation);
				getArtifactsPonderation(resolvedArtifact.getTransitiveDependencies(), result);
			}
		}
	}

	/**
	 * Instantiates a new abstract pom sync wizard page.
	 * 
	 * @param chosenProject
	 *            the chosen project
	 * @param projectNames
	 *            the project names
	 * @param libraries
	 *            the libraries
	 * @param wizardId
	 *            the wizard id
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 * @param columnsNumber
	 *            the columns number
	 */
	public AbstractPomSyncWizardPage(IProject chosenProject, String[] projectNames, Set<AbstractChosenArtifactVersion> libraries, String wizardId, String title, String description, int columnsNumber) {
		super(wizardId, title, description, columnsNumber);
		this.PROJECT_NAMES = projectNames;
		if (chosenProject != null) {
			this.CHOSENPROJECTNAME = chosenProject.getName();
			setChosenProjectName(this.CHOSENPROJECTNAME);
		} else {
			CHOSENPROJECTNAME = null;
			if (PROJECT_NAMES.length == 1) {
				setChosenProjectName(PROJECT_NAMES[0]);
			}
		}
		this.LIBRARIES = libraries;
		this.ARTIFACTS_PONDERATION = getArtifactsPonderation(LIBRARIES);
		// this.CONSIDER_OPTIONAL = AggregatedProperties.getDealWithOptional(chosenProject);
	}

	/**
	 * Instantiates a new abstract pom sync wizard page.
	 * 
	 * @param chosenProject
	 *            the chosen project
	 * @param projectNames
	 *            the project names
	 * @param libraries
	 *            the libraries
	 * @param wizardId
	 *            the wizard id
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 */
	public AbstractPomSyncWizardPage(String chosenProject, String[] projectNames, Set<AbstractChosenArtifactVersion> libraries, String wizardId, String title, String description) {
		super(wizardId, title, description);
		this.PROJECT_NAMES = projectNames;
		if (chosenProject != null) {
			this.CHOSENPROJECTNAME = chosenProject;
			setChosenProjectName(this.CHOSENPROJECTNAME);
		} else {
			CHOSENPROJECTNAME = null;
			if (PROJECT_NAMES.length == 1) {
				setChosenProjectName(PROJECT_NAMES[0]);
			}
		}
		this.LIBRARIES = libraries;
		this.ARTIFACTS_PONDERATION = getArtifactsPonderation(LIBRARIES);
		// this.CONSIDER_OPTIONAL = AggregatedProperties.getDealWithOptional(chosenProject);
	}

	/**
	 * Gets the repository.
	 * 
	 * @return the repository
	 */
	public CrawledRepository getRepository() {
		return this.result;
	}

	/**
	 * Gets the chosen project name.
	 * 
	 * @return the chosen project name
	 */
	public String getChosenProjectName() {
		return chosenProjectName;
	}

	/**
	 * Refresh library fields.
	 */
	public abstract void refreshLibraryFields();

	/**
	 * Removes the selected elements.
	 * 
	 * @param treeListDialogField
	 *            the tree list dialog field
	 */
	protected void removeSelectedElements(TreeListDialogField treeListDialogField) {
		AbstractChosenArtifactVersion abstractChosenArtifactVersion = (AbstractChosenArtifactVersion) treeListDialogField.getSelectedElements().get(0);
		if (abstractChosenArtifactVersion instanceof ResolvedArtifact) {
			ResolvedArtifact resolvedArtifact = (ResolvedArtifact) abstractChosenArtifactVersion;
			if (resolvedArtifact != null) {
				treeListDialogField.removeElements(treeListDialogField.getSelectedElements());
				validateLibraries = true;
				String groupId = (resolvedArtifact.getArtifactVersion().getParent().getParent()).getName();
				String artifactId = resolvedArtifact.getArtifactVersion().getParent().getUID();
				String key = groupId + "|" + artifactId; //$NON-NLS-1$
				int ponderation = ARTIFACTS_PONDERATION.get(key) == null ? 0 : ARTIFACTS_PONDERATION.get(key);
				ARTIFACTS_PONDERATION.put(key, --ponderation);
				refreshLibraryFields();
			}
		} else if (abstractChosenArtifactVersion instanceof UnresolvedArtifact) {
			WarningDialog warningDialog = new WarningDialog(WizardsMessages.AbstractPomSyncWizardPage_warning_remove_unresolved_title, WizardsMessages.AbstractPomSyncWizardPage_warning_remove_unresolved_message);
			warningDialog.open();
		}
	}

	/**
	 * Sets the chosen project name.
	 * 
	 * @param chosenProjectName
	 *            the new chosen project name
	 */
	protected void setChosenProjectName(String chosenProjectName) {
		this.chosenProjectName = chosenProjectName;
	}

	/**
	 * Gets the validate libraries.
	 * 
	 * @return the validate libraries
	 */
	protected Boolean getValidateLibraries() {
		return validateLibraries;
	}

	/**
	 * Sets the validate libraries.
	 * 
	 * @param validateLibraries
	 *            the new validate libraries
	 */
	protected void setValidateLibraries(Boolean validateLibraries) {
		this.validateLibraries = validateLibraries;
	}

	/**
	 * Are libraries ok.
	 * 
	 * @return true, if successful
	 */
	protected boolean areLibrariesOk() {
		return librariesOk;
	}

	/**
	 * Sets the libraries ok.
	 * 
	 * @param librariesOk
	 *            the new libraries ok
	 */
	protected void setLibrariesOk(boolean librariesOk) {
		this.librariesOk = librariesOk;
	}

	/**
	 * Describe not found section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	protected Composite describeNotFoundSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(1, true);
		composite.setLayout(layout);
		issuesLibrariesDialogField = new ListDialogField(getWizardAdapter(), null, new LibrariesLabelProvider(this));
		issuesLibrariesDialogField.setLabelText(WizardsMessages.AbstractPomSyncWizardPage_issuesLibraries);
		issuesLibrariesDialogField.setEnabled(true);
		issuesLibrariesDialogField.doFillIntoTable(composite, 3);
		issuesLibrariesDialogField.getListControl(null).setMenu(createNotFoundMenu());
		return composite;
	}

	/**
	 * Creates the not found menu.
	 * 
	 * @return the menu
	 */
	private Menu createNotFoundMenu() {
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(WizardsMessages.AbstractPomSyncWizardPage_ignoreInProject);
		menuItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ignoreSelectedElements(issuesLibrariesDialogField);
			}

			public void widgetSelected(SelectionEvent e) {
				ignoreSelectedElements(issuesLibrariesDialogField);
			}
		});
		return menu;
	}

	/**
	 * Ignore selected elements.
	 * 
	 * @param issuesLibrariesDialogField2
	 *            the issues libraries dialog field2
	 */
	private void ignoreSelectedElements(ListDialogField issuesLibrariesDialogField2) {
		IProject project = FileToolBox.getProject(chosenProjectName);
		try {
			Set<SkippedDependency> skippedDependencies = AggregatedProperties.getSkippedDependencies(project);
			for (Object selectedElement : issuesLibrariesDialogField2.getSelectedElements()) {
				UnresolvedArtifact unresolvedArtifact = (UnresolvedArtifact) selectedElement;
				skippedDependencies.add(new SkippedDependency(unresolvedArtifact.getUnresolvedPomDependency().getGroupId(), unresolvedArtifact.getUnresolvedPomDependency().getArtifactId()));
			}
			PropertiesFacade.setProjectProperty(project, PropertiesNames.P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES, AggregatedProperties.formatSkippedDependencies(skippedDependencies));
			PropertiesFacade.storePropertiesToFile(project);
			try {
				reOpenWizardWithSameProject();
			} catch (WizardInitException e) {
				PomJavaSynchronizationHandler.manageWizardInitException(WizardsMessages.AbstractPomSyncWizardPage_sync, e);
				closeWizard();
			}
		} catch (CoreException e) {
			ErrorDialog errorDialog = new ErrorDialog(WizardsMessages.AbstractPomSyncWizardPage_error_properties_update_title, WizardsMessages.AbstractPomSyncWizardPage_error_properties_update_message, e);
			errorDialog.open();
		}
	}

	/**
	 * Re open wizard.
	 */
	protected abstract void reOpenWizardWithSameProject();

	/**
	 * Close wizard.
	 */
	protected void closeWizard() {
		getWizard().getContainer().getShell().close();
	}

	/**
	 * Describe conflicting section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	protected Composite describeConflictingSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		removeConflictingDialogField = new SelectionButtonDialogField(SWT.CHECK);
		removeConflictingDialogField.setLabelText(WizardsMessages.AbstractPomSyncWizardPage_removed_from_classpath);
		removeConflictingDialogField.setSelection(false);
		removeConflictingDialogField.setEnabled(true);
		removeConflictingDialogField.doFillIntoTable(composite, 1);
		conflictingLibrariesDialogField = new TreeListDialogField(new ConflictingListAdapter(this), null, new LibrariesLabelProvider(this));
		conflictingLibrariesDialogField.setLabelText(""); //$NON-NLS-1$
		conflictingLibrariesDialogField.setEnabled(true);
		conflictingLibrariesDialogField.doFillIntoTable(composite, 3);
		return composite;
	}
}
