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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.eclipse.dws.ui.DWSUIPlugin;
import org.org.eclipse.dws.ui.internal.images.PluginImages;
import org.org.eclipse.dws.ui.internal.views.actions.AddFileSystemRepositoryAction;
import org.org.eclipse.dws.ui.internal.views.actions.AddHttpRepositoryAction;
import org.org.eclipse.dws.ui.internal.views.actions.AddPreciseGroupToRepositoryAction;
import org.org.eclipse.dws.ui.internal.views.actions.AddToLibraryPackAction;
import org.org.eclipse.dws.ui.internal.views.actions.AddTransitiveArtifactsAction;
import org.org.eclipse.dws.ui.internal.views.actions.CopyDependencyInfoAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadAndAddToClasspathAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadAndAddToFolderAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadAndAddToLocalRepositoryAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadAndAddToWebAppAction;
import org.org.eclipse.dws.ui.internal.views.actions.DownloadToLocalFileSystemAction;
import org.org.eclipse.dws.ui.internal.views.actions.EditRepositoryAction;
import org.org.eclipse.dws.ui.internal.views.actions.ExportRepositoryAction;
import org.org.eclipse.dws.ui.internal.views.actions.ImportRepositoryFromFileAction;
import org.org.eclipse.dws.ui.internal.views.actions.ImportRepositoryFromUrlAction;
import org.org.eclipse.dws.ui.internal.views.actions.ListTransitiveDependenciesAction;
import org.org.eclipse.dws.ui.internal.views.actions.RefreshItemAction;
import org.org.eclipse.dws.ui.internal.views.actions.RemoveRepositoryAction;
import org.org.eclipse.dws.ui.internal.views.actions.ShowDependencyInfoAction;
import org.org.eclipse.dws.ui.internal.views.actions.ShowDetailsAction;
import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.CrawledRepository;
import org.org.repository.crawler.maven2.model.Group;

/**
 * This view shows data obtained from the model. The sample creates a dummy model on the fly, but a real implementation would connect to the model available either in this or another plug-in (e.g. the workspace). The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be presented in the view. Each view can present the same model objects using different labels and icons, if needed. Alternatively, a single label provider can be shared between views in order to ensure that objects of the same type are presented in the same way everywhere.
 * <p>
 */
public class MavenRepositoriesView extends ViewPart implements IActionHost {

	private static MavenRepositoriesView view = null;

	private TreeViewer viewer;

	private final static String CONTEXT_MENU_ID = MavenRepositoriesView.class.getName() + "#contextmenu";

	private static final String VIEW_ID = MavenRepositoriesView.class.getName();

	private DownloadAndAddToClasspathAction downloadAndAddToClasspathAction;

	private DownloadAndAddToFolderAction downloadAndAddToFolderAction;

	private DownloadAndAddToWebAppAction downloadAndAddToWebAppAction;

	private DownloadAndAddToLocalRepositoryAction downloadAndAddToLocalRepositoryAction;

	private DownloadToLocalFileSystemAction downloadToLocalFileSystemAction;

	private RefreshItemAction refreshItemAction;

	private AddHttpRepositoryAction addHttpRepositoryAction;

	private AddFileSystemRepositoryAction addFileSystemRepositoryAction;

	private AddToLibraryPackAction addToLibraryPackAction;

	private EditRepositoryAction editRepositoryAction;

	private RemoveRepositoryAction removeRepositoryAction;

	private IToolBarManager toolBarManager;

	private Label filterLabel;

	private Text filterField;

	private MavenRepositoriesViewFilter filter;

	private ShowDependencyInfoAction showDependencyInfoAction;

	private ListTransitiveDependenciesAction listTransitiveDependenciesAction;

	private ExportRepositoryAction exportRepositoryAction;

	private ImportRepositoryFromFileAction importRepositoryFromFileAction;

	private ImportRepositoryFromUrlAction importRepositoryFromUrlAction;

	private CopyDependencyInfoAction copyDependencyInfoAction;

	// private AddGroupFiltersToRepositoryAction addGroupFiltersToRepositoryAction;

	private ShowDetailsAction showDetailsAction;

	private Clipboard clipboard;

	private AddPreciseGroupToRepositoryAction addPreciseGroupsToRepositoryAction;

	private AddTransitiveArtifactsAction addTransitiveArtifactsAction;

	private ToolTip tooltip;

	public TreeViewer getViewer() {
		return viewer;
	}

	static class NameSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			int result = 0;
			if (e1 instanceof CrawledRepository && e2 instanceof CrawledRepository) {
				result = ((CrawledRepository) e1).getUID().compareTo(((CrawledRepository) e2).getUID());
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
			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
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
	public MavenRepositoriesView() {
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
		viewer.setContentProvider(new MavenRepositoriesViewContentProvider(getViewSite()));
		viewer.setLabelProvider(new DecoratingLabelProvider(new MavenRepositoriesViewLabelProvider(), new MavenRepositoriesViewLabelDecorator()));
		viewer.setSorter(new NameSorter());
		filter = new MavenRepositoriesViewFilter();
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
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				showDetailsAction.run();
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
		tooltip = new MavenRepositoriesViewToolTip(viewer.getTree());
		tooltip.setPopupDelay(1000);
	}

	protected void touchFilter() {
		filter.setUserFilter(filterField.getText());
		refreshViewerKeepStatus();
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

		downloadAndAddToLocalRepositoryAction = new DownloadAndAddToLocalRepositoryAction(this);
		downloadAndAddToLocalRepositoryAction.setText("Add to local repository");
		downloadAndAddToLocalRepositoryAction.setToolTipText("Download libraries and add them to the local repositories.");
		downloadAndAddToLocalRepositoryAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16));
		downloadAndAddToLocalRepositoryAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16));
		downloadAndAddToLocalRepositoryAction.setResolver(new DownloadAndAddToLocalRepositoryResolver(this));

		downloadToLocalFileSystemAction = new DownloadToLocalFileSystemAction(this);
		downloadToLocalFileSystemAction.setText("Add to local File system");
		downloadToLocalFileSystemAction.setToolTipText("Download libraries and add them to the local repositories.");
		downloadToLocalFileSystemAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16));
		downloadToLocalFileSystemAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16));
		downloadToLocalFileSystemAction.setResolver(new DownloadToLocalFileSystemResolver(this));

		showDependencyInfoAction = new ShowDependencyInfoAction(this);
		showDependencyInfoAction.setText("Show dependency info");
		showDependencyInfoAction.setToolTipText("Shows the maven2 XML description for this dependency.");
		showDependencyInfoAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		showDependencyInfoAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		showDependencyInfoAction.setResolver(new ShowDependencyInfoActionResolver(this));

		showDetailsAction = new ShowDetailsAction(this);
		showDetailsAction.setText("Show details");
		showDetailsAction.setToolTipText("Shows the details of this item in the Details view.");
		showDetailsAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		showDetailsAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		showDetailsAction.setResolver(new ShowDetailsActionResolver(this));

		listTransitiveDependenciesAction = new ListTransitiveDependenciesAction(this);
		listTransitiveDependenciesAction.setText("List transitive dependencies");
		listTransitiveDependenciesAction.setToolTipText("Lists the transitive dependencies with compile scope.");
		listTransitiveDependenciesAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		listTransitiveDependenciesAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		listTransitiveDependenciesAction.setResolver(new ListTransitiveDependenciesActionResolver(this));

		addTransitiveArtifactsAction = new AddTransitiveArtifactsAction(this);
		addTransitiveArtifactsAction.setText("Add transitive to repository");
		addTransitiveArtifactsAction.setToolTipText("Adds the transitive dependencies with compile scope to the repository.");
		addTransitiveArtifactsAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16));
		addTransitiveArtifactsAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_DOWNLOAD_TO_LOCAL_16));
		addTransitiveArtifactsAction.setResolver(new RetrieveTransitiveArtifactsActionResolver(this));

		refreshItemAction = new RefreshItemAction(this);
		refreshItemAction.setText("Refresh");
		refreshItemAction.setToolTipText("Refresh this item.");
		refreshItemAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_REFRESH_16));
		refreshItemAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_REFRESH_16));
		refreshItemAction.setResolver(new RefreshItemActionResolver(this));

		editRepositoryAction = new EditRepositoryAction(this);
		editRepositoryAction.setText("Edit");
		editRepositoryAction.setToolTipText("Edit CrawledRepository.");
		editRepositoryAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_EDIT_16));
		editRepositoryAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_EDIT_16));
		editRepositoryAction.setResolver(new EditRepositoryActionResolver(this));

		addHttpRepositoryAction = new AddHttpRepositoryAction(this);
		addHttpRepositoryAction.setText("Add Http CrawledRepository");
		addHttpRepositoryAction.setToolTipText("Add a new Http-browsable CrawledRepository to the list.");
		addHttpRepositoryAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ADD_HTTP_16));
		addHttpRepositoryAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ADD_HTTP_16));
		addHttpRepositoryAction.setResolver(new AddRepositoryActionResolver(this));

		addToLibraryPackAction = new AddToLibraryPackAction(this);
		addToLibraryPackAction.setText("Add to Library Pack");
		addToLibraryPackAction.setToolTipText("Add to an existing or to a new Library Pack");
		addToLibraryPackAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACT_VERSION_LIBRARY_TYPE));
		addToLibraryPackAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACT_VERSION_LIBRARY_TYPE));
		addToLibraryPackAction.setResolver(new AddToLibraryPackActionResolver(this));

		addFileSystemRepositoryAction = new AddFileSystemRepositoryAction(this);
		addFileSystemRepositoryAction.setText("Add File System CrawledRepository");
		addFileSystemRepositoryAction.setToolTipText("Add a new File System-browsable CrawledRepository to the list.");
		addFileSystemRepositoryAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ADD_FILESYSTEM_16));
		addFileSystemRepositoryAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ADD_FILESYSTEM_16));
		addFileSystemRepositoryAction.setResolver(new AddRepositoryActionResolver(this));

		removeRepositoryAction = new RemoveRepositoryAction(this);
		removeRepositoryAction.setText("Remove");
		removeRepositoryAction.setToolTipText("Removes the repository(ies) from the list.");
		removeRepositoryAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_REMOVE_16));
		removeRepositoryAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_REMOVE_16));
		removeRepositoryAction.setResolver(new RemoveRepositoryActionResolver(this));

		exportRepositoryAction = new ExportRepositoryAction(this);
		exportRepositoryAction.setText("Export to xml");
		exportRepositoryAction.setToolTipText("Export the repository to a xml format.");
		exportRepositoryAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_EXPORT_16));
		exportRepositoryAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_EXPORT_16));
		exportRepositoryAction.setResolver(new ExportRepositoryActionResolver(this));

		importRepositoryFromFileAction = new ImportRepositoryFromFileAction(this);
		importRepositoryFromFileAction.setText("Import from xml file");
		importRepositoryFromFileAction.setToolTipText("Import repositories from an xml format.");
		importRepositoryFromFileAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_IMPORT_16));
		importRepositoryFromFileAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_IMPORT_16));
		importRepositoryFromFileAction.setResolver(new ImportRepositoryActionResolver(this));

		importRepositoryFromUrlAction = new ImportRepositoryFromUrlAction(this);
		importRepositoryFromUrlAction.setText("Import from remote xml file");
		importRepositoryFromUrlAction.setToolTipText("Import repositories from a remote xml format.");
		importRepositoryFromUrlAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_IMPORT_URL_16));
		importRepositoryFromUrlAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_IMPORT_URL_16));
		importRepositoryFromUrlAction.setResolver(new ImportRepositoryActionResolver(this));

		copyDependencyInfoAction = new CopyDependencyInfoAction(this, clipboard);
		copyDependencyInfoAction.setText("Copy dependency Info");
		copyDependencyInfoAction.setToolTipText("Copy XML Dependency format to the clipboard.");
		copyDependencyInfoAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		copyDependencyInfoAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_ARTIFACTVERSION_16));
		copyDependencyInfoAction.setResolver(new CopyDependencyInfoActionResolver(this));

		// addGroupFiltersToRepositoryAction = new AddGroupFiltersToRepositoryAction(this);
		// addGroupFiltersToRepositoryAction.setText("Add Group Filter");
		// addGroupFiltersToRepositoryAction.setToolTipText("Add a group filter (like \"org\" for example).");
		// addGroupFiltersToRepositoryAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_GROUP_16));
		// addGroupFiltersToRepositoryAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_GROUP_16));
		// addGroupFiltersToRepositoryAction.setResolver(new AddGroupFiltersToRepositoryActionResolver(this));

		addPreciseGroupsToRepositoryAction = new AddPreciseGroupToRepositoryAction(this);
		addPreciseGroupsToRepositoryAction.setText("Add Precise Group");
		addPreciseGroupsToRepositoryAction.setToolTipText("Add a precise group to browse.");
		addPreciseGroupsToRepositoryAction.setImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_GROUP_16));
		addPreciseGroupsToRepositoryAction.setDisabledImageDescriptor(DWSUIPlugin.getDefault().getImages().getImageDescriptor(PluginImages.LOGO_MAVEN_GROUP_16));
		addPreciseGroupsToRepositoryAction.setResolver(new AddPreciseGroupToRepositoryActionResolver(this));
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
		toolBarManager.add(addHttpRepositoryAction);
		toolBarManager.add(addFileSystemRepositoryAction);
		toolBarManager.add(importRepositoryFromFileAction);
		toolBarManager.add(importRepositoryFromUrlAction);
		toolBarManager.update(true);
	}

	private void refreshToolBar() {
		toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.removeAll();
		toolBarManager.add(addHttpRepositoryAction);
		toolBarManager.add(addFileSystemRepositoryAction);
		toolBarManager.add(importRepositoryFromFileAction);
		toolBarManager.add(importRepositoryFromUrlAction);
		toolBarManager.update(true);
	}

	private void fillContextMenu(IMenuManager menuMgr) {
		if (removeRepositoryAction.isEnabled()) {
			menuMgr.add(removeRepositoryAction);
		}
		if (editRepositoryAction.isEnabled()) {
			menuMgr.add(editRepositoryAction);
		}
		if (addPreciseGroupsToRepositoryAction.isEnabled()) {
			menuMgr.add(addPreciseGroupsToRepositoryAction);
		}
		if (refreshItemAction.isEnabled()) {
			menuMgr.add(refreshItemAction);
		}
		if (exportRepositoryAction.isEnabled()) {
			menuMgr.add(exportRepositoryAction);
		}
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
		if (downloadAndAddToLocalRepositoryAction.isEnabled()) {
			subMenuManager.add(downloadAndAddToLocalRepositoryAction);
		}
		if (addToLibraryPackAction.isEnabled()) {
			subMenuManager.add(addToLibraryPackAction);
		}
		menuMgr.add(subMenuManager);
		subMenuManager = new MenuManager("Info...");
		if (showDependencyInfoAction.isEnabled()) {
			subMenuManager.add(showDependencyInfoAction);
		}
		if (showDetailsAction.isEnabled()) {
			subMenuManager.add(showDetailsAction);
		}
		if (copyDependencyInfoAction.isEnabled()) {
			subMenuManager.add(copyDependencyInfoAction);
		}
		if (listTransitiveDependenciesAction.isEnabled()) {
			subMenuManager.add(listTransitiveDependenciesAction);
		}
		if (addTransitiveArtifactsAction.isEnabled()) {
			subMenuManager.add(addTransitiveArtifactsAction);
		}
		menuMgr.add(subMenuManager);

		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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
					RepositoryModelPersistence.checkStatus();
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

	public static void refreshViewerKeepStatus() {
		if (view != null) {
			view.getViewer().getTree().getDisplay().asyncExec(new Runnable() {
				public void run() {
					RepositoryModelPersistence.checkStatus();
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

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof ArtifactVersion)) {
					result = false;
					break;
				} else {
					if (((ArtifactVersion) modelItem).getType().equals(ArtifactVersion.Type.LIBRARY)) {
						result = true;
					} else {
						result = false;
						break;
					}
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

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof ArtifactVersion)) {
					result = false;
					break;
				} else {
					result = true;
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

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof ArtifactVersion)) {
					result = false;
					break;
				} else {
					if (((ArtifactVersion) modelItem).getType().equals(ArtifactVersion.Type.LIBRARY)) {
						result = true;
					} else {
						result = false;
						break;
					}
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

		@SuppressWarnings("rawtypes")
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

	private static class DownloadAndAddToLocalRepositoryResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public DownloadAndAddToLocalRepositoryResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
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

	private static class ShowDependencyInfoActionResolver implements IActionResolver {
		@SuppressWarnings("unused")
		private IActionHost actionHost;

		/**
		 * 
		 */
		public ShowDependencyInfoActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		public boolean isEnabled() {
			return false;
		}
	}

	private static class ListTransitiveDependenciesActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public ListTransitiveDependenciesActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof ArtifactVersion)) {
					result = false;
					break;
				} else {
					if (((ArtifactVersion) modelItem).getPomUrl() != null) {
						result = true;
					} else {
						result = false;
					}
				}
			}
			return result;
		}
	}

	private static class RetrieveTransitiveArtifactsActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public RetrieveTransitiveArtifactsActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof ArtifactVersion)) {
					result = false;
					break;
				} else {
					if (((ArtifactVersion) modelItem).getPomUrl() != null) {
						result = true;
					} else {
						result = false;
					}
				}
			}
			return result;
		}
	}

	private static class RefreshItemActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public RefreshItemActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (modelItem instanceof ArtifactVersion || modelItem instanceof Artifact) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			return result;
		}
	}

	private static class EditRepositoryActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public EditRepositoryActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			IModelItem modelItem = (IModelItem) selection.getFirstElement();
			if (modelItem instanceof CrawledRepository) {
				result = true;
			}
			if (selection.size() > 1) {
				result = false;
			}
			return result;
		}
	}

	private static class AddRepositoryActionResolver implements IActionResolver {
		// private IActionHost actionHost;

		/**
		 * 
		 */
		public AddRepositoryActionResolver(IActionHost actionHost) {
			// this.actionHost = actionHost;
		}

		public boolean isEnabled() {
			return true;
		}
	}

	private static class AddToLibraryPackActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public AddToLibraryPackActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof ArtifactVersion)) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			return result;
		}
	}

	private static class ImportRepositoryActionResolver implements IActionResolver {
		// private IActionHost actionHost;

		/**
		 * 
		 */
		public ImportRepositoryActionResolver(IActionHost actionHost) {
			// this.actionHost = actionHost;
		}

		public boolean isEnabled() {
			return true;
		}
	}

	private static class RemoveRepositoryActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public RemoveRepositoryActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof CrawledRepository)) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			return result;
		}
	}

	private static class ExportRepositoryActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public ExportRepositoryActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof CrawledRepository)) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			return result;
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

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof ArtifactVersion)) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			return result;
		}
	}

	// private static class AddGroupFiltersToRepositoryActionResolver implements IActionResolver {
	// private IActionHost actionHost;
	//
	// /**
	// *
	// */
	// public AddGroupFiltersToRepositoryActionResolver(IActionHost actionHost) {
	// this.actionHost = actionHost;
	// }
	//
	// public boolean isEnabled() {
	// IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
	// boolean result = false;
	// for (Iterator it = selection.iterator(); it.hasNext();) {
	// IModelItem modelItem = (IModelItem) it.next();
	// if (!(modelItem instanceof CrawledRepository)) {
	// result = false;
	// break;
	// } else {
	// result = true;
	// }
	// }
	// return result;
	// }
	// }

	private static class AddPreciseGroupToRepositoryActionResolver implements IActionResolver {
		private IActionHost actionHost;

		/**
		 * 
		 */
		public AddPreciseGroupToRepositoryActionResolver(IActionHost actionHost) {
			this.actionHost = actionHost;
		}

		@SuppressWarnings("rawtypes")
		public boolean isEnabled() {
			IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
			boolean result = false;
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (!(modelItem instanceof CrawledRepository)) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			return result;
		}
	}

	public void setEnabled(boolean enabled) {
		this.viewer.getControl().setEnabled(enabled);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void showViewAndFocusOnElement(IModelItem item) {
		try {
			IWorkbenchPage activePage = DWSUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			activePage.showView(MavenRepositoriesView.VIEW_ID);
		} catch (PartInitException pie) {
			ErrorDialog errorDialog = new ErrorDialog("Impossible to open DWS Repositories view", "Impossible to open DWS Repositories view", pie);
			errorDialog.open();
		}
		if (item != null && view != null) {
			final List list = new ArrayList();
			list.add(item);
			view.getViewer().setSelection(new IStructuredSelection() {

				public boolean isEmpty() {
					return false;
				}

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