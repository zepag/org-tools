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
import java.util.TreeSet;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.org.eclipse.cheatsheet.catalog.CheatSheetCatalogPlugin;
import org.org.eclipse.cheatsheet.catalog.internal.images.PluginImages;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetCatalog;
import org.org.eclipse.cheatsheet.catalog.internal.model.CheatSheetReference;
import org.org.eclipse.cheatsheet.catalog.internal.model.ModelConstants;
import org.org.eclipse.cheatsheet.catalog.internal.model.Tags;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.AddCatalogAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.AddCategoryAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.AddFSReferenceAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.AddHttpReferenceAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.AddPlatformReferenceAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.DeleteElementAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.DuplicateCatalogAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.EditCatalogAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.EditCategoryAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.EditFSReferenceAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.EditHttpReferenceAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.EditPlatformReferenceAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.ExportCatalogAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.ImportCatalogFromFileAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.ImportCatalogFromUrlAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.LockSwitchAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.OpenReferenceAction;
import org.org.eclipse.cheatsheet.catalog.internal.views.actions.RefreshCatalogAction;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;
import org.org.eclipse.core.utils.platform.views.actions.ResolvedAction;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.model.RootModelItem;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view shows data obtained from the model. The sample creates a dummy model on the fly, but a real implementation would connect to the model available either in this or another plug-in (e.g. the workspace). The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be presented in the view. Each view can present the same model objects using different labels and icons, if needed. Alternatively, a single label provider can be shared between views in order to ensure that objects of the same type are presented in the same way everywhere.
 * <p>
 */
@SuppressWarnings("rawtypes")
public class CheatSheetCatalogView extends ViewPart {

	public class TagHarvestingVisitor implements IModelItemVisitor {
		Set<String> allTags = new TreeSet<String>();

		public boolean visit(IModelItem modelItem) {
			boolean keepOnVisitingChildren = true;
			if (modelItem instanceof CheatSheetReference) {
				keepOnVisitingChildren = false;
				Tags tags = ((CheatSheetReference) modelItem).getTags();
				if (tags != null) {
					for (String tag : tags.getTagsArray()) {
						allTags.add(tag);
					}
				}
			}
			return keepOnVisitingChildren;
		}

		public String[] getAllTags() {
			return allTags.toArray(new String[] {});
		}

	}

	public static final String VIEW_ID = CheatSheetCatalogView.class.getName();

	private TreeViewer viewer;
	private ResolvedAction<CheatSheetCatalogView> openReferenceAction;
	private ResolvedAction<CheatSheetCatalogView> addCatalogAction;
	private ResolvedAction<CheatSheetCatalogView> exportCatalogAction;
	private ResolvedAction<CheatSheetCatalogView> importCatalogFromFileAction;
	private ResolvedAction<CheatSheetCatalogView> importCatalogFromUrlAction;
	private ResolvedAction<CheatSheetCatalogView> deleteElementAction;
	private ResolvedAction<CheatSheetCatalogView> addCategoryAction;
	private ResolvedAction<CheatSheetCatalogView> addReferenceAction;
	private ResolvedAction<CheatSheetCatalogView> editCatalogAction;
	private ResolvedAction<CheatSheetCatalogView> refreshCatalogAction;
	private ResolvedAction<CheatSheetCatalogView> editCategoryAction;
	private ResolvedAction<CheatSheetCatalogView> editHttpReferenceAction;
	private ResolvedAction<CheatSheetCatalogView> duplicateCatalogAction;
	private ResolvedAction<CheatSheetCatalogView> lockSwitchAction;
	private ResolvedAction<CheatSheetCatalogView> addFSReferenceAction;
	private ResolvedAction<CheatSheetCatalogView> addPlatformReferenceAction;

	private ToolTip tooltip;

	private Label filterLabel;

	private Combo filterField;

	private CheatSheetCatalogViewTagBasedFilter filter;

	private EditFSReferenceAction editFSReferenceAction;

	private EditPlatformReferenceAction editPlatformReferenceAction;

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public CheatSheetCatalogView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
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
		filterLabel.setText("Tags Filter :");
		filterLabel.setToolTipText("Cheat sheet references are tagged by theme.");
		filterLabel.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
		filterLabel.setLayoutData(new GridData(GridData.BEGINNING));
		filterField = new Combo(filterZone, SWT.BORDER);
		filterField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				touchFilter();
			}
		});
		RootModelItem<CheatSheetCatalog> root = RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID);
		TagHarvestingVisitor visitor = new TagHarvestingVisitor();
		root.accept(visitor);
		String[] comboChoices = new String[visitor.getAllTags().length + 1];
		comboChoices[0] = "";
		for (int i = 1; i < comboChoices.length; i++) {
			comboChoices[i] = visitor.getAllTags()[i - 1];
		}
		filterField.setItems(comboChoices);
		filterField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider(this, viewer));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		filter = new CheatSheetCatalogViewTagBasedFilter();
		viewer.addFilter(filter);
		viewer.setInput(getViewSite());
		createActions();
		hookDoubleClickAction();
		hookContextMenu();
		contributeToActionBars();
		viewer.getTree().setToolTipText("");
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		tooltip = new CheatCheatCatalogViewToolTip(viewer.getTree());
		tooltip.setPopupDelay(1000);
	}

	protected void touchFilter() {
		if (filter != null) {
			int selectionIndex = filterField.getSelectionIndex();
			String text = filterField.getText();
			if (selectionIndex != -1) {
				filter.setUserFilter(filterField.getItem(selectionIndex));
			} else {
				filter.setUserFilter(text);
			}
		}
		if (viewer != null) {

			refreshViewerKeepFolding();
		}
	}

	public void touchTags() {
		RootModelItem<CheatSheetCatalog> root = RootModelItem.<CheatSheetCatalog> getInstance(ModelConstants.ROOT_MODEL_ITEM_ID);
		TagHarvestingVisitor visitor = new TagHarvestingVisitor();
		root.accept(visitor);
		String[] comboChoices = new String[visitor.getAllTags().length + 1];
		comboChoices[0] = "";
		for (int i = 1; i < comboChoices.length; i++) {
			comboChoices[i] = visitor.getAllTags()[i - 1];
		}
		filterField.setItems(comboChoices);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					if (((IStructuredSelection) event.getSelection()).getFirstElement() instanceof CheatSheetReference) {
						openReferenceAction.run();
					}
				}
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu2");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CheatSheetCatalogView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
		fillLocalPullDown(bars.getMenuManager());
		bars.getMenuManager().addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CheatSheetCatalogView.this.fillLocalPullDown(manager);
			}
		});
	}

	private void fillContextMenu(IMenuManager manager) {
		if (openReferenceAction.isEnabled()) {
			manager.add(openReferenceAction);
		}
		if (editCatalogAction.isEnabled()) {
			manager.add(editCatalogAction);
		}
		if (refreshCatalogAction.isEnabled()) {
			manager.add(refreshCatalogAction);
		}
		if (editCategoryAction.isEnabled()) {
			manager.add(editCategoryAction);
		}
		if (editHttpReferenceAction.isEnabled()) {
			manager.add(editHttpReferenceAction);
		}
		if (editFSReferenceAction.isEnabled()) {
			manager.add(editFSReferenceAction);
		}
		if (editPlatformReferenceAction.isEnabled()) {
			manager.add(editPlatformReferenceAction);
		}
		if (addCategoryAction.isEnabled()) {
			manager.add(addCategoryAction);
		}
		if (addReferenceAction.isEnabled()) {
			manager.add(addReferenceAction);
		}
		if (addFSReferenceAction.isEnabled()) {
			manager.add(addFSReferenceAction);
		}
		if (addPlatformReferenceAction.isEnabled()) {
			manager.add(addPlatformReferenceAction);
		}
		if (deleteElementAction.isEnabled()) {
			manager.add(deleteElementAction);
		}
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(importCatalogFromFileAction);
		manager.add(importCatalogFromUrlAction);
		manager.add(new Separator());
	}

	private void fillLocalPullDown(IMenuManager menuManager) {
		menuManager.removeAll();
		menuManager.add(addCatalogAction);
		if (duplicateCatalogAction.isEnabled()) {
			menuManager.add(duplicateCatalogAction);
		}
		if (lockSwitchAction.isEnabled()) {
			menuManager.add(lockSwitchAction);
		}
		if (exportCatalogAction.isEnabled()) {
			menuManager.add(exportCatalogAction);
		}
		menuManager.add(new Separator());
	}

	private void createActions() {
		openReferenceAction = new OpenReferenceAction(this, this);
		openReferenceAction.setText("Open");
		openReferenceAction.setToolTipText("Opens the referenced Cheat Sheet.");
		openReferenceAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.OPEN_CHEATSHEET_REFERENCE));

		exportCatalogAction = new ExportCatalogAction(this, this);
		exportCatalogAction.setText("Export");
		exportCatalogAction.setToolTipText("Export the catalog as XML.");
		exportCatalogAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.EXPORT));

		addCatalogAction = new AddCatalogAction(this, this);
		addCatalogAction.setText("Create new Catalog");
		addCatalogAction.setToolTipText("Create a new Cheat Sheets Catalog.");
		addCatalogAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.ADD_CHEATSHEET_CATALOG));

		addCategoryAction = new AddCategoryAction(this, this);
		addCategoryAction.setText("Add Category");
		addCategoryAction.setToolTipText("Add a new Cheat Sheets Category to this catalog.");
		addCategoryAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.ADD_CHEATSHEET_CATEGORY));

		addReferenceAction = new AddHttpReferenceAction(this, this);
		addReferenceAction.setText("Add Reference");
		addReferenceAction.setToolTipText("Add a new Cheat Sheets Reference to this catalog.");
		addReferenceAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.ADD_CHEATSHEET_REFERENCE));

		addFSReferenceAction = new AddFSReferenceAction(this, this);
		addFSReferenceAction.setText("Add File System Reference");
		addFSReferenceAction.setToolTipText("Add a new Cheat Sheets Reference to this catalog. This references a file in the local file system.");
		addFSReferenceAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.ADD_CHEATSHEET_REFERENCE));

		addPlatformReferenceAction = new AddPlatformReferenceAction(this, this);
		addPlatformReferenceAction.setText("Add Platform Reference");
		addPlatformReferenceAction.setToolTipText("Add a new Cheat Sheets Reference to this catalog. This references a cheatsheet contributed by plugins in the platform.");
		addPlatformReferenceAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.ADD_CHEATSHEET_REFERENCE));

		editCatalogAction = new EditCatalogAction(this, this);
		editCatalogAction.setText("Edit");
		editCatalogAction.setToolTipText("Edit this catalog's properties.");
		editCatalogAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.EDIT_CHEATSHEET_CATALOG));

		refreshCatalogAction = new RefreshCatalogAction(this, this);
		refreshCatalogAction.setText("Refresh");
		refreshCatalogAction.setToolTipText("Refresh catalog from its reference");
		refreshCatalogAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.REFRESH));

		editCategoryAction = new EditCategoryAction(this, this);
		editCategoryAction.setText("Edit");
		editCategoryAction.setToolTipText("Edit this category's properties.");
		editCategoryAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.EDIT_CHEATSHEET_CATEGORY));

		editHttpReferenceAction = new EditHttpReferenceAction(this, this);
		editHttpReferenceAction.setText("Edit");
		editHttpReferenceAction.setToolTipText("Edit this reference's properties.");
		editHttpReferenceAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.EDIT_CHEATSHEET_REFERENCE));

		editFSReferenceAction = new EditFSReferenceAction(this, this);
		editFSReferenceAction.setText("Edit");
		editFSReferenceAction.setToolTipText("Edit this reference's properties.");
		editFSReferenceAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.EDIT_CHEATSHEET_REFERENCE));

		editPlatformReferenceAction = new EditPlatformReferenceAction(this, this);
		editPlatformReferenceAction.setText("Edit");
		editPlatformReferenceAction.setToolTipText("Edit this reference's properties.");
		editPlatformReferenceAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.EDIT_CHEATSHEET_REFERENCE));

		importCatalogFromFileAction = new ImportCatalogFromFileAction(this, this);
		importCatalogFromFileAction.setText("File System Import");
		importCatalogFromFileAction.setToolTipText("Import a catalog from an XML file in the file system.");
		importCatalogFromFileAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.IMPORT));

		importCatalogFromUrlAction = new ImportCatalogFromUrlAction(this, this);
		importCatalogFromUrlAction.setText("File System Import");
		importCatalogFromUrlAction.setToolTipText("Import a catalog from a remote XML file through http.");
		importCatalogFromUrlAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.IMPORT_FROM_URL));

		duplicateCatalogAction = new DuplicateCatalogAction(this, this);
		duplicateCatalogAction.setText("Duplicate");
		duplicateCatalogAction.setToolTipText("Duplicate this catalog.");
		duplicateCatalogAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.DUPLICATE));

		lockSwitchAction = new LockSwitchAction(this, this);
		lockSwitchAction.setText("Lock / Unlock");
		lockSwitchAction.setToolTipText("Switches catalog's status between readonly and writable.");

		deleteElementAction = new DeleteElementAction(this, this);
		deleteElementAction.setText("Delete");
		deleteElementAction.setToolTipText("Delete this CheatSheetCatalog.");
		deleteElementAction.setImageDescriptor(CheatSheetCatalogPlugin.getPluginImageDescriptor(PluginImages.DELETE));

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	public static IViewPart showView() {
		try {
			return CheatSheetCatalogPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CheatSheetCatalogView.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE/* view is only opened but not focused */);
		} catch (PartInitException pie) {
			ErrorDialog errorDialog = new ErrorDialog("Impossible to open DWS CheatSheet catalogs view", "Impossible to open DWS CheatSheet catalogs view", pie);
			errorDialog.open();
		}
		return null;
	}

	public void refreshViewerKeepFolding() {
		viewer.getTree().getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh(true);
				Object[] expandedElements = viewer.getExpandedElements();
				// WORK AROUND for Bug 103747 of SWT's TreeViewer
				viewer.collapseAll();
				// END OF WORK AROUND
				viewer.setExpandedElements(expandedElements);
			}
		});
	}

}