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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.bridges.LibraryPackModelPersistence;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion.Target;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.eclipse.dws.ui.internal.images.PluginImages;
import org.org.eclipse.dws.ui.internal.views.actions.AddTargetToLibraryPackArtifactVersionAction;
import org.org.eclipse.dws.ui.internal.views.actions.CopyDependencyInfoAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadAndAddToClasspathAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadAndAddToFolderAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadAndAddToWebAppAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadToLocalFileSystemAction;
import org.org.eclipse.dws.ui.internal.views.actions.RemoveFromLibraryPackAction;
import org.org.eclipse.dws.ui.internal.views.actions.RemoveLibraryPackAction;
import org.org.eclipse.dws.ui.internal.views.actions.RemoveTargetToLibraryPackArtifactVersionAction;
import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;

/**
 * This view shows data obtained from the model. The sample creates a dummy model on the fly, but a real implementation would connect to the model available either in this or another plug-in (e.g. the workspace). The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be presented in the view. Each view can present the same model objects using different labels and icons, if needed. Alternatively, a single label provider can be shared between views in order to ensure that objects of the same type are presented in the same way everywhere.
 * <p>
 */
public class LibraryPacksView extends ViewPart implements IActionHost {

	private static LibraryPacksView view = null;

	private TreeViewer viewer;

	private final static String CONTEXT_MENU_ID = LibraryPacksView.class.getName() + "#contextmenu";

	private static final String VIEW_ID = LibraryPacksView.class.getName();

	private DownloadAndAddToClasspathAction downloadAndAddToClasspathAction;

	private DownloadAndAddToFolderAction downloadAndAddToFolderAction;

	private DownloadAndAddToWebAppAction downloadAndAddToWebAppAction;

	private DownloadToLocalFileSystemAction downloadToLocalFileSystemAction;

	private IToolBarManager toolBarManager;

	private Label filterLabel;

	private Text filterField;

	private LibraryPacksViewFilter filter;

	private CopyDependencyInfoAction copyDependencyInfoAction;

	private Clipboard clipboard;

	private ToolTip tooltip;

	private RemoveLibraryPackAction removeLibraryPackAction;

	private RemoveFromLibraryPackAction removeFromLibraryPackAction;

	private List<AddTargetToLibraryPackArtifactVersionAction> addTargetActions = new ArrayList<AddTargetToLibraryPackArtifactVersionAction>();

	private List<RemoveTargetToLibraryPackArtifactVersionAction> removeTargetActions = new ArrayList<RemoveTargetToLibraryPackArtifactVersionAction>();

	public TreeViewer getViewer() {
		return viewer;
	}

	static class NameSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			int result = 0;
			if (e1 instanceof LibraryPack && e2 instanceof LibraryPack) {
				result = ((LibraryPack) e1).getLabel().compareTo(((LibraryPack) e2).getLabel());
			} else if (e1 instanceof Group && e2 instanceof Group) {
				result = ((Group) e1).getName().compareTo(((Group) e2).getName());
			} else if (e1 instanceof Artifact && e2 instanceof Artifact) {
				result = ((Artifact) e1).getId().compareTo(((Artifact) e2).getId());
			} else if (e1 instanceof ArtifactVersion && e2 instanceof ArtifactVersion) {
				result = -((ArtifactVersion) e1).getVersion().compareTo(((ArtifactVersion) e2).getVersion());
			} else {
				result = super.compare(viewer, e1, e2);
			}
			return result;
		}
	}

	static class LibraryTransferListener implements DragSourceListener {

		private ArtifactVersion[] data;

		private TreeViewer viewer;

		public LibraryTransferListener(TreeViewer viewer) {
			this.viewer = viewer;
		}

		public void dragFinished(DragSourceEvent event) {
			if (!event.doit)
				return;
		}

		public void dragSetData(DragSourceEvent event) {
			StringBuilder builder = new StringBuilder();
			for (ArtifactVersion artifactVersion : data) {
				builder.append(PomInteractionHelper.toDependencyXML(artifactVersion));
			}
			event.data = builder.toString();
		}

		@SuppressWarnings("unchecked")
		public void dragStart(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			boolean allArtifactVersions = true;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				if (!(it.next() instanceof ArtifactVersion)) {
					allArtifactVersions = false;
					break;
				}
			}
			event.doit = !viewer.getSelection().isEmpty() && allArtifactVersions;
			data = (ArtifactVersion[]) selection.toList().toArray(new ArtifactVersion[selection.size()]);
		}

	}

	/**
	 * The constructor.
	 */
	public LibraryPacksView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		parent.setLayout(layout);
		parent.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		Composite filterZone = new Composite(parent, SWT.FLAT);
		filterZone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterZone.setLayout(new GridLayout(2, false));
		filterZone.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		filterLabel = new Label(filterZone, SWT.FLAT);
		filterLabel.setText("Artifact Filter :");
		filterLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		filterLabel.setLayoutData(new GridData(GridData.BEGINNING));
		filterField = new Text(filterZone, SWT.BORDER);
		filterField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				touchFilter();
			}
		});
		filterField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new LibraryPacksViewContentProvider(getViewSite()));
		viewer.setLabelProvider(new DecoratingLabelProvider(new LibraryPacksViewLabelProvider(), new LibraryPacksViewLabelDecorator()));
		viewer.setSorter(new NameSorter());
		filter = new LibraryPacksViewFilter();
		viewer.addFilter(filter);

		int ops = DND.DROP_COPY;
		Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };
		viewer.addDragSupport(ops, transfers, new LibraryTransferListener(viewer));
		viewer.setInput(getViewSite());
		viewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		view = this;
		clipboard = new Clipboard(getSite().getShell().getDisplay());
		initializeActions();
		createContextMenu();
		createToolBar();
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copyDependencyInfoAction);
		getViewSite().getActionBars().updateActionBars();
		getViewSite().setSelectionProvider(viewer);
		tooltip = new LibraryPacksViewToolTip(viewer.getTree());
		tooltip.setPopupDelay(1000);
	}

	protected void touchFilter() {
		filter.setUserFilter(filterField.getText());
		refreshViewersKeepStatus();
	}

	private void handleKeyPressed(KeyEvent event) {
	}

	private void initializeActions() {
		downloadAndAddToClasspathAction = new DownloadAndAddToClasspathAction(this);
		downloadAndAddToClasspathAction.setText("Add to Build Path");
		downloadAndAddToClasspathAction.setToolTipText("Download this artifact and add it to the Build Path.");
		downloadAndAddToClasspathAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_16));
		downloadAndAddToClasspathAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_16));
		downloadAndAddToClasspathAction.setResolver(new DownloadAndAddToClasspathActionResolver(this));

		downloadAndAddToFolderAction = new DownloadAndAddToFolderAction(this);
		downloadAndAddToFolderAction.setText("Add to a project folder");
		downloadAndAddToFolderAction.setToolTipText("Download this artifact and add it to a project folder.");
		downloadAndAddToFolderAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_16));
		downloadAndAddToFolderAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_16));
		downloadAndAddToFolderAction.setResolver(new DownloadAndAddToFolderActionResolver(this));

		downloadAndAddToWebAppAction = new DownloadAndAddToWebAppAction(this);
		downloadAndAddToWebAppAction.setText("Add to WEB-INF/lib");
		downloadAndAddToWebAppAction.setToolTipText("Download this artifact and add it to the WEB-INF/lib folder.");
		downloadAndAddToWebAppAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_16));
		downloadAndAddToWebAppAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_16));
		downloadAndAddToWebAppAction.setResolver(new DownloadAndAddToWebAppActionResolver(this));

		downloadToLocalFileSystemAction = new DownloadToLocalFileSystemAction(this);
		downloadToLocalFileSystemAction.setText("Add to local File system");
		downloadToLocalFileSystemAction.setToolTipText("Download libraries and add them to the local repositories.");
		downloadToLocalFileSystemAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16));
		downloadToLocalFileSystemAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16));
		downloadToLocalFileSystemAction.setResolver(new DownloadToLocalFileSystemResolver(this));

		copyDependencyInfoAction = new CopyDependencyInfoAction(this, clipboard);
		copyDependencyInfoAction.setText("Copy dependency Info");
		copyDependencyInfoAction.setToolTipText("Copy XML Dependency format to the clipboard.");
		copyDependencyInfoAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		copyDependencyInfoAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		copyDependencyInfoAction.setResolver(new CopyDependencyInfoActionResolver(this));

		removeLibraryPackAction = new RemoveLibraryPackAction(this);
		removeLibraryPackAction.setText("Remove Library Pack");
		removeLibraryPackAction.setToolTipText("Removes the library pack. If it is contributed through an extension point, it will be back on next startup.");
		removeLibraryPackAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_REMOVE_16));
		removeLibraryPackAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_REMOVE_16));
		removeLibraryPackAction.setResolver(new RemoveLibraryPackActionResolver(this));

		removeFromLibraryPackAction = new RemoveFromLibraryPackAction(this);
		removeFromLibraryPackAction.setText("Remove from Library Pack");
		removeFromLibraryPackAction.setToolTipText("Removes artifacts from the library pack. If the library pack is contributed through an extension point, it will be back on next startup.");
		removeFromLibraryPackAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_REMOVE_16));
		removeFromLibraryPackAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_REMOVE_16));
		removeFromLibraryPackAction.setResolver(new RemoveFromLibraryPackActionResolver(this));

		for (Target target : Target.values()) {
			AddTargetToLibraryPackArtifactVersionAction addTargetAction = new AddTargetToLibraryPackArtifactVersionAction(target, this);
			addTargetAction.setText(target.name());
			addTargetAction.setToolTipText("Adds the " + target.name() + " target to this library.");
			addTargetAction.setResolver(new AddTargetToLibraryPackArtifactVersionActionResolver(target, this));
			addTargetActions.add(addTargetAction);
			RemoveTargetToLibraryPackArtifactVersionAction removeTargetAction = new RemoveTargetToLibraryPackArtifactVersionAction(target, this);
			removeTargetAction.setText(target.name());
			removeTargetAction.setToolTipText("Removes the " + target.name() + " target to this library.");
			removeTargetAction.setResolver(new RemoveTargetToLibraryPackArtifactVersionActionResolver(target, this));
			removeTargetActions.add(removeTargetAction);
		}
	}

	private void createContextMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		// Register the context menu such that other plugins may contribute
		getSite().registerContextMenu(CONTEXT_MENU_ID, menuMgr, viewer);
	}

	private void createToolBar() {
		toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.update(true);
	}

	private void refreshToolBar() {
		toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.removeAll();
		toolBarManager.update(true);
	}

	private void fillContextMenu(IMenuManager menuMgr) {
		if (removeLibraryPackAction.isEnabled()) {
			menuMgr.add(removeLibraryPackAction);
		}
		if (removeFromLibraryPackAction.isEnabled()) {
			menuMgr.add(removeFromLibraryPackAction);
		}
		fillAddToContextMenu(menuMgr);
		fillInfoContextMenu(menuMgr);
		fillTargetAdditionContextMenu(menuMgr);
		fillTargetRemovalContextMenu(menuMgr);

		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillTargetRemovalContextMenu(IMenuManager menuMgr) {
		List<RemoveTargetToLibraryPackArtifactVersionAction> chosenRemoveActions = new ArrayList<RemoveTargetToLibraryPackArtifactVersionAction>();
		for (RemoveTargetToLibraryPackArtifactVersionAction removeTargetAction : removeTargetActions) {
			if (removeTargetAction.isEnabled()) {
				chosenRemoveActions.add(removeTargetAction);
			}
		}
		if (chosenRemoveActions.size() > 0) {
			MenuManager subMenuManager = new MenuManager("Remove target...");
			for (RemoveTargetToLibraryPackArtifactVersionAction removeTargetAction : chosenRemoveActions) {
				subMenuManager.add(removeTargetAction);
			}
			menuMgr.add(subMenuManager);
		}
	}

	private void fillTargetAdditionContextMenu(IMenuManager menuMgr) {
		List<AddTargetToLibraryPackArtifactVersionAction> chosenAddActions = new ArrayList<AddTargetToLibraryPackArtifactVersionAction>();
		for (AddTargetToLibraryPackArtifactVersionAction addTargetAction : addTargetActions) {
			if (addTargetAction.isEnabled()) {
				chosenAddActions.add(addTargetAction);
			}
		}
		if (chosenAddActions.size() > 0) {
			MenuManager subMenuManager = new MenuManager("Add target...");
			for (AddTargetToLibraryPackArtifactVersionAction addTargetAction : chosenAddActions) {
				subMenuManager.add(addTargetAction);
			}
			menuMgr.add(subMenuManager);
		}
	}

	private void fillInfoContextMenu(IMenuManager menuMgr) {
		MenuManager subMenuManager = new MenuManager("Info...");
		if (copyDependencyInfoAction.isEnabled()) {
			subMenuManager.add(copyDependencyInfoAction);
		}
		menuMgr.add(subMenuManager);
	}

	private void fillAddToContextMenu(IMenuManager menuMgr) {
		MenuManager subMenuManager = new MenuManager("Add to...");
		if (downloadAndAddToClasspathAction.isEnabled()) {
			subMenuManager.add(downloadAndAddToClasspathAction);
		}
		if (downloadAndAddToFolderAction.isEnabled()) {
			subMenuManager.add(downloadAndAddToFolderAction);
		}
		if (downloadAndAddToWebAppAction.isEnabled()) {
			subMenuManager.add(downloadAndAddToWebAppAction);
		}
		if (downloadToLocalFileSystemAction.isEnabled()) {
			subMenuManager.add(downloadToLocalFileSystemAction);
		}
		menuMgr.add(subMenuManager);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public static void refreshViewer() {
		if (view != null) {
			view.getViewer().getTree().getDisplay().asyncExec(new Runnable() {
				public void run() {
					LibraryPackModelPersistence.checkStatus();
					try {
						view.getViewer().refresh(true);
					} catch (Exception e) {
						System.err.println(e);
					}
					view.refreshToolBar();
				}
			});
		}
	}

	public static void refreshViewersKeepStatus() {
		if (view != null) {
			view.getViewer().getTree().getDisplay().asyncExec(new Runnable() {
				public void run() {
					LibraryPackModelPersistence.checkStatus();
					view.getViewer().refresh(true);
					Object[] expandedElements = view.getViewer().getExpandedElements();
					// WORK AROUND for Bug 103747 of SWT's TreeViewer
					view.getViewer().collapseAll();
					// END OF WORK AROUND
					view.getViewer().setExpandedElements(expandedElements);
					view.refreshToolBar();
				}
			});
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		viewer.getTree().dispose();
		view = null;
		clipboard.dispose();
	}

	public Object getActionTrigger() {
		return getViewer().getSelection();
	}

	public Shell getShell() {
		return viewer.getControl().getShell();
	}

	public IToolBarManager getToolBarManager() {
		return toolBarManager;
	}

	private static class DownloadAndAddToClasspathActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public DownloadAndAddToClasspathActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = true;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object modelItem = it.next();
				if (!(modelItem instanceof IModelItem)) {
					result = false;
					break;
				}
			}
			return result;
		}
	}

	private static class DownloadAndAddToFolderActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public DownloadAndAddToFolderActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = true;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object modelItem = it.next();
				if (!(modelItem instanceof IModelItem)) {
					result = false;
					break;
				}
			}
			return result;
		}
	}

	private static class DownloadAndAddToWebAppActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public DownloadAndAddToWebAppActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = true;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object modelItem = it.next();
				if (!(modelItem instanceof IModelItem)) {
					result = false;
					break;
				}
			}
			return result;
		}
	}

	private static class DownloadToLocalFileSystemResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public DownloadToLocalFileSystemResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				if (!(it.next() instanceof IModelItem)) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			return result;
		}
	}

	@SuppressWarnings("unused")
	private static class ShowDetailsActionResolver implements IActionResolver {
		@SuppressWarnings("unused")
		private IActionHost actionHost;

		/**
		 * 
		 */
		public ShowDetailsActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		public boolean isEnabled() {
			return true;
		}
	}

	private static class CopyDependencyInfoActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public CopyDependencyInfoActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = true;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object modelItem = it.next();
				if (!(modelItem instanceof IModelItem)) {
					result = false;
					break;
				}
			}
			return result;
		}
	}

	private static class RemoveLibraryPackActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public RemoveLibraryPackActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = true;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object modelItem = it.next();
				if (!(modelItem instanceof LibraryPack)) {
					result = false;
					break;
				}
			}
			return result;
		}
	}

	private static class RemoveFromLibraryPackActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public RemoveFromLibraryPackActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = true;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object modelItem = it.next();
				if (modelItem instanceof LibraryPack) {
					result = false;
					break;
				}
			}
			return result;
		}
	}

	private static class AddTargetToLibraryPackArtifactVersionActionResolver implements IActionResolver {
		private IActionHost actionHost;
		private final Target target;

		/**
		 * 
		 */
		public AddTargetToLibraryPackArtifactVersionActionResolver(Target target, IActionHost actionHost) {
			this.actionHost = actionHost;
			this.target = target;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object modelItem = it.next();
				if (modelItem instanceof LibraryPackArtifactVersion) {
					LibraryPackArtifactVersion artifactVersion = (LibraryPackArtifactVersion) modelItem;
					if (!(artifactVersion.getTargets().contains(target))) {
						result = true;
						break;
					}
				}
			}
			return result;
		}
	}

	private static class RemoveTargetToLibraryPackArtifactVersionActionResolver implements IActionResolver {
		private IActionHost actionHost;
		private final Target target;

		/**
		 * 
		 */
		public RemoveTargetToLibraryPackArtifactVersionActionResolver(Target target, IActionHost actionHost) {
			this.actionHost = actionHost;
			this.target = target;
		}

		@SuppressWarnings("unchecked")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object modelItem = it.next();
				if (modelItem instanceof LibraryPackArtifactVersion) {
					LibraryPackArtifactVersion artifactVersion = (LibraryPackArtifactVersion) modelItem;
					if (artifactVersion.getTargets().contains(target)) {
						result = true;
						break;
					}
				}
			}
			return result;
		}
	}

	public void setEnabled(boolean enabled) {
		this.viewer.getControl().setEnabled(enabled);
	}

	@SuppressWarnings("unchecked")
	public static void showViewAndFocusOnElement(IModelItem item) {
		try {
			IWorkbenchPage activePage = DWSUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			activePage.showView(LibraryPacksView.VIEW_ID);
		} catch (PartInitException pie) {
			ErrorDialog errorDialog = new ErrorDialog("Impossible to open DWS Library Packs view", "Impossible to open DWS Library Packs view", pie);
			errorDialog.open();
		}
		if (item != null && view != null) {
			final List list = new ArrayList();
			list.add(item);
			view.getViewer().setSelection(new IStructuredSelection() {

				public boolean isEmpty() {
					return false;
				}

				@SuppressWarnings("unchecked")
				public List toList() {
					return list;
				}

				public Object[] toArray() {
					return list.toArray();
				}

				public int size() {
					return list.size();
				}

				public Iterator iterator() {
					return list.iterator();
				}

				public Object getFirstElement() {
					return list.get(0);
				}

			}, true);

		}
	}
}