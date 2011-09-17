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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.org.eclipse.core.utils.platform.PlatformUtilsPlugin;
import org.org.eclipse.core.utils.platform.dialogs.selection.ComboSelectionDialog;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.ComboDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogFieldListener;
import org.org.eclipse.core.utils.platform.fields.ITreeListAdapter;
import org.org.eclipse.core.utils.platform.fields.SelectionButtonDialogField;
import org.org.eclipse.core.utils.platform.fields.TreeListDialogField;
import org.org.eclipse.core.utils.platform.images.PluginImages;
import org.org.eclipse.core.utils.platform.tools.FileToolBox;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.core.utils.platform.wizards.page.WizardContentsHelper;
import org.org.eclipse.dws.core.internal.DependenciesHelper;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.DependenciesHelper.SearchContext;
import org.org.eclipse.dws.core.internal.PomInteractionHelper.PomInteractionException;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationConstants;
import org.org.eclipse.dws.core.internal.configuration.preferences.GeneralPreferencePage;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.ResolvedArtifact;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.ui.internal.wizards.AbstractPomSynchronizationWizard;
import org.org.eclipse.dws.ui.internal.wizards.PomWebAppSynchronizationWizard;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException.Status;

/**
 * The Class PomWebAppSynchronizationWizardPage.
 */
public class PomWebAppSynchronizationWizardPage extends AbstractPomSyncWizardPage {

	/**
	 * The Class ClasspathListAdapter.
	 */
	public class ClasspathListAdapter implements ITreeListAdapter {

		/**
		 * Checks for children.
		 * 
		 * @param field
		 *            the field
		 * @param element
		 *            the element
		 * 
		 * @return true, if checks for children
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#hasChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public boolean hasChildren(TreeListDialogField field, Object element) {
			return false;
		}

		/**
		 * Gets the parent.
		 * 
		 * @param field
		 *            the field
		 * @param element
		 *            the element
		 * 
		 * @return the parent
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getParent(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public Object getParent(TreeListDialogField field, Object element) {
			return null;
		}

		/**
		 * Gets the children.
		 * 
		 * @param field
		 *            the field
		 * @param element
		 *            the element
		 * 
		 * @return the children
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#getChildren(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, java.lang.Object)
		 */
		public Object[] getChildren(TreeListDialogField field, Object element) {
			return null;
		}

		/**
		 * Key pressed.
		 * 
		 * @param field
		 *            the field
		 * @param e
		 *            the e
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#keyPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(TreeListDialogField field, KeyEvent e) {
			if (e.keyCode == 127 || e.keyCode == 8) {
				ResolvedArtifact resolvedArtifact = (ResolvedArtifact) librariesDialogField.getSelectedElements().get(0);
				if (resolvedArtifact != null) {
					librariesDialogField.removeElements(librariesDialogField.getSelectedElements());
					setValidateLibraries(true);
					String groupId = (resolvedArtifact.getArtifactVersion().getParent().getParent()).getName();
					String artifactId = resolvedArtifact.getArtifactVersion().getParent().getUID();
					String key = groupId + "|" + artifactId; //$NON-NLS-1$
					int ponderation = ARTIFACTS_PONDERATION.get(key) == null ? 0 : ARTIFACTS_PONDERATION.get(key);
					ARTIFACTS_PONDERATION.put(key, --ponderation);
					refreshLibraryFields();
				}
			}
		}

		/**
		 * Double clicked.
		 * 
		 * @param field
		 *            the field
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#doubleClicked(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void doubleClicked(TreeListDialogField field) {
		}

		/**
		 * Selection changed.
		 * 
		 * @param field
		 *            the field
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#selectionChanged(org.org.eclipse.core.utils.platform.fields.TreeListDialogField)
		 */
		public void selectionChanged(TreeListDialogField field) {
			touch();
		}

		/**
		 * Custom button pressed.
		 * 
		 * @param field
		 *            the field
		 * @param index
		 *            the index
		 * 
		 * @see org.org.eclipse.core.utils.platform.fields.ITreeListAdapter#customButtonPressed(org.org.eclipse.core.utils.platform.fields.TreeListDialogField, int)
		 */
		public void customButtonPressed(TreeListDialogField field, int index) {
			// will never happen...
		}

	}

	/** The Constant WIZARD_PAGE_ID. */
	public static final String WIZARD_PAGE_ID = PomWebAppSynchronizationWizardPage.class.getName();

	/** The Constant PREFERENCES_LINK_LABEL. */
	private static final String PREFERENCES_LINK_LABEL = WizardsMessages.PomWebAppSynchronizationWizardPage_preferences_link;

	/** The project dialog field. */
	private ComboDialogField projectDialogField;

	/** The libraries dialog field. */
	private TreeListDialogField librariesDialogField;

	/** The web app dialog field. */
	private TreeListDialogField webAppDialogField;

	/** The undetermined libraries dialog field. */
	private TreeListDialogField undeterminedLibrariesDialogField;

	/** The keep undetermined dialog field. */
	private SelectionButtonDialogField keepUndeterminedDialogField;

	/** The label. */
	private Label label;

	/** The preferences link. */
	private Link preferencesLink;

	/**
	 * Instantiates a new pom web app synchronization wizard page.
	 * 
	 * @param projectNames
	 *            the project names
	 * @param libraries
	 *            the libraries
	 * @param chosenProject
	 *            the chosen project
	 */
	public PomWebAppSynchronizationWizardPage(String[] projectNames, Set<AbstractChosenArtifactVersion> libraries, String chosenProject) {
		super(chosenProject, projectNames, libraries, WIZARD_PAGE_ID, WizardsMessages.PomWebAppSynchronizationWizardPage_projectAndLibs, WizardsMessages.PomWebAppSynchronizationWizardPage_chooseLibraries);
		setColumnsNumber(1);
	}

	/**
	 * Describe.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#describe()
	 */
	@Override
	protected void describe() {
		IProject project = null;
		if (PROJECT_NAMES.length == 1) {
			project = FileToolBox.getProject(PROJECT_NAMES[0]);
		} else if (CHOSENPROJECTNAME != null) {
			project = FileToolBox.getProject(CHOSENPROJECTNAME);
		}
		if (project != null && project.exists() && !AggregatedProperties.useWorkspacePreferences(project)) {
			label = new Label(getWizardContainer(), SWT.FLAT | SWT.BORDER);
			label.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_preferences_from_project + project.getName());
		} else {
			Composite composite = new Composite(getWizardContainer(), SWT.BORDER);
			composite.setLayout(new GridLayout(2, false));
			label = new Label(composite, SWT.FLAT);
			label.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_workspace_preferences);
			preferencesLink = new Link(composite, SWT.NONE);
			preferencesLink.setFont(composite.getFont());
			preferencesLink.setText("<A>" + PREFERENCES_LINK_LABEL + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$
			preferencesLink.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					doLinkActivated((Link) e.widget);
				}

				private void doLinkActivated(Link link) {
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), GeneralPreferencePage.class.getName(), null, null);
					dialog.open();
					reOpenWizardWithSameProject();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					doLinkActivated((Link) e.widget);
				}
			});
		}
		ExpandBar expandBar = WizardContentsHelper.createExpandBar(getWizardContainer());
		expandBar.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite targetProjectComposite = describeTargetProjectSection(project, expandBar);
		targetProjectComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		WizardContentsHelper.createExpandItem(expandBar, targetProjectComposite, WizardsMessages.PomWebAppSynchronizationWizardPage_targetProject, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));

		SearchContext searchContext = new SearchContext();
		searchContext.setDealWithTransitive(AggregatedProperties.getDealWithTransitive(project));
		searchContext.setDealWithOptional(AggregatedProperties.getDealWithOptional(project));
		searchContext.setDealWithUnknownOrRestrictiveScope(AggregatedProperties.getDealWithNarrow(project));
		if (DependenciesHelper.unresolvedLibraries(searchContext, LIBRARIES)) {
			Composite notFoundComposite = describeNotFoundSection(expandBar);
			notFoundComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			WizardContentsHelper.createExpandItem(expandBar, notFoundComposite, WizardsMessages.PomWebAppSynchronizationWizardPage_libraries_with_issues, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_ERROR_16));
		}
		if (DependenciesHelper.resolvedArtifacts(searchContext, LIBRARIES)) {
			Composite webInfComposite = describeWebInfSection(expandBar);
			webInfComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			WizardContentsHelper.createExpandItem(expandBar, webInfComposite, WizardsMessages.PomWebAppSynchronizationWizardPage_librariesAddedToWEBINFLIB, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));

			Composite classpathComposite = describeClasspathSection(expandBar);
			classpathComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			WizardContentsHelper.createExpandItem(expandBar, classpathComposite, WizardsMessages.PomWebAppSynchronizationWizardPage_librariesAddedToClasspath, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
		}
		if (DependenciesHelper.transitiveDependencies(searchContext, LIBRARIES)) {
			Boolean automaticallyAddToClasspath = AggregatedProperties.getAutomaticallyAddTransitive(project);
			Composite composite = describeTransitiveDependenciesSection(expandBar, WizardsMessages.PomWebAppSynchronizationWizardPage_addTransitiveLibrariesToWEBINFLIB);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			ExpandItem transitiveDependenciesSection = WizardContentsHelper.createExpandItem(expandBar, composite, WizardsMessages.PomWebAppSynchronizationWizardPage_transitiveDependencies, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
			if (!automaticallyAddToClasspath) {
				transitiveDependenciesSection.setImage(PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_WARNING_16));
			}
		}
		if (DependenciesHelper.containsConflictingClasspathEntries(searchContext, LIBRARIES)) {
			Boolean automaticallyRemoveFromClasspath = AggregatedProperties.getAutomaticallyRemoveConflicting(project);
			Composite composite = describeConflictingSection(expandBar);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			ExpandItem conflictingLibrariesSection = WizardContentsHelper.createExpandItem(expandBar, composite, WizardsMessages.PomWebAppSynchronizationWizardPage_librariesConflicting, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
			if (!automaticallyRemoveFromClasspath) {
				conflictingLibrariesSection.setImage(PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_WARNING_16));
			}
		}
		if (DependenciesHelper.unknownOrRestrictedScope(searchContext, LIBRARIES)) {
			Boolean automaticallyAddUnknown = AggregatedProperties.getAutomaticallyAddUnknown(project);
			Composite composite = describeUnknownSection(expandBar);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			ExpandItem undeterminedLibrariesSection = WizardContentsHelper.createExpandItem(expandBar, composite, WizardsMessages.PomWebAppSynchronizationWizardPage_undeterminedLibraries, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
			if (!automaticallyAddUnknown) {
				undeterminedLibrariesSection.setImage(PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_WARNING_16));
			}
		}
	}

	/**
	 * Describe unknown section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describeUnknownSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(1, true);
		composite.setLayout(layout);
		keepUndeterminedDialogField = new SelectionButtonDialogField(SWT.CHECK);
		keepUndeterminedDialogField.setLabelText(WizardsMessages.PomWebAppSynchronizationWizardPage_addToClasspathCheckbox);
		keepUndeterminedDialogField.setSelection(false);
		keepUndeterminedDialogField.setEnabled(true);
		keepUndeterminedDialogField.doFillIntoTable(composite, 1);
		undeterminedLibrariesDialogField = new TreeListDialogField(new UndeterminedListAdapter(), null, new LibrariesLabelProvider(this));
		undeterminedLibrariesDialogField.setLabelText(WizardsMessages.PomWebAppSynchronizationWizardPage_addToClasspathDescription);
		undeterminedLibrariesDialogField.setEnabled(true);
		undeterminedLibrariesDialogField.doFillIntoTable(composite, 3);
		return composite;
	}

	/**
	 * Describe transitive dependencies section.
	 * 
	 * @param parent
	 *            the parent
	 * @param label
	 *            the label
	 * 
	 * @return the composite
	 */
	private Composite describeTransitiveDependenciesSection(Composite parent, String label) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(1, true);
		composite.setLayout(layout);
		addTransitiveDependenciesField = new SelectionButtonDialogField(SWT.CHECK);
		addTransitiveDependenciesField.setLabelText(label);
		addTransitiveDependenciesField.setSelection(false);
		addTransitiveDependenciesField.setEnabled(true);
		addTransitiveDependenciesField.doFillIntoTable(composite, 1);
		transitiveDependenciesDialogField = new TreeListDialogField(new TransitiveListAdapter(this), null, new LibrariesLabelProvider(this));
		transitiveDependenciesDialogField.setLabelText(WizardsMessages.PomWebAppSynchronizationWizardPage_transitiveLibrariesToWEBINFLIBDescription);
		transitiveDependenciesDialogField.setEnabled(true);
		transitiveDependenciesDialogField.setTreeExpansionLevel(10);
		transitiveDependenciesDialogField.doFillIntoTable(composite, 3);
		transitiveDependenciesDialogField.getTreeControl(null).setMenu(createTransitiveDependenciesMenu());
		return composite;
	}

	/**
	 * Describe classpath section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describeClasspathSection(Composite parent) {
		Composite classpathComposite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(1, true);
		classpathComposite.setLayout(layout);
		Label artifactSelectLabel = new Label(classpathComposite, SWT.FLAT);
		artifactSelectLabel.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_selectAndPressDEL);
		librariesDialogField = new TreeListDialogField(new ClasspathListAdapter(), null, new LibrariesLabelProvider(this));
		librariesDialogField.setLabelText(WizardsMessages.PomWebAppSynchronizationWizardPage_libraries);
		librariesDialogField.setEnabled(true);
		librariesDialogField.doFillIntoTable(classpathComposite, 3);
		librariesDialogField.getTreeControl(null).setMenu(createClasspathMenu());
		return classpathComposite;
	}

	/**
	 * Describe web inf section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describeWebInfSection(Composite parent) {
		Composite webInfComposite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(1, true);
		webInfComposite.setLayout(layout);
		Label label = new Label(webInfComposite, SWT.NONE);
		label.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_selectAndPressDEL);
		webAppDialogField = new TreeListDialogField(new LibrariesListAdapter(this), null, new LibrariesLabelProvider(this));
		webAppDialogField.setLabelText(WizardsMessages.PomWebAppSynchronizationWizardPage_libraries);
		webAppDialogField.setEnabled(true);
		webAppDialogField.doFillIntoTable(webInfComposite, 3);
		webAppDialogField.getTreeControl(null).setMenu(createWebAppMenu());
		return webInfComposite;
	}

	/**
	 * Describe target project section.
	 * 
	 * @param project
	 *            the project
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describeTargetProjectSection(IProject project, Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(2, true);
		composite.setLayout(layout);

		projectDialogField = new ComboDialogField(SWT.READ_ONLY);
		projectDialogField.setLabelText(WizardsMessages.PomWebAppSynchronizationWizardPage_chooseFromJavaProject);
		projectDialogField.setDialogFieldListener(getWizardAdapter());
		projectDialogField.doFillIntoTable(composite, 2);
		if (project != null && project.exists()) {
			projectDialogField.selectItem(project.getName());
		}

		return composite;
	}

	/**
	 * Creates the classpath menu.
	 * 
	 * @return the menu
	 */
	private Menu createClasspathMenu() {
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_moveToWEBINFLIB);
		menuItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				moveArtifactToLibrary();
			}

			private void moveArtifactToLibrary() {
				List<?> selectedElements = librariesDialogField.getSelectedElements();
				webAppDialogField.addElements(selectedElements);
				librariesDialogField.removeElements(selectedElements);
			}

			public void widgetSelected(SelectionEvent e) {
				moveArtifactToLibrary();
			}
		});
		MenuItem menuItem2 = new MenuItem(menu, SWT.PUSH);
		menuItem2.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_remove);
		menuItem2.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				removeSelectedElements(librariesDialogField);
			}

			public void widgetSelected(SelectionEvent e) {
				removeSelectedElements(librariesDialogField);
			}
		});
		return menu;
	}

	/**
	 * Creates the web app menu.
	 * 
	 * @return the menu
	 */
	private Menu createWebAppMenu() {
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_moveToClasspath);
		menuItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				moveArtifactToClasspath();
			}

			private void moveArtifactToClasspath() {
				List<?> selectedElements = webAppDialogField.getSelectedElements();
				librariesDialogField.addElements(selectedElements);
				webAppDialogField.removeElements(selectedElements);
			}

			public void widgetSelected(SelectionEvent e) {
				moveArtifactToClasspath();
			}
		});
		MenuItem menuItem2 = new MenuItem(menu, SWT.PUSH);
		menuItem2.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_remove);
		menuItem2.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				removeSelectedElements(webAppDialogField);
			}

			public void widgetSelected(SelectionEvent e) {
				removeSelectedElements(webAppDialogField);
			}
		});
		return menu;
	}

	/**
	 * Creates the transitive dependencies menu.
	 * 
	 * @return the menu
	 */
	private Menu createTransitiveDependenciesMenu() {
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(WizardsMessages.PomWebAppSynchronizationWizardPage_remove);
		menuItem.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				removeSelectedElements(transitiveDependenciesDialogField);
			}

			public void widgetSelected(SelectionEvent e) {
				removeSelectedElements(transitiveDependenciesDialogField);
			}
		});
		return menu;
	}

	/**
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#initialize()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void initialize() {
		IProject project = null;
		if (PROJECT_NAMES.length == 1) {
			project = FileToolBox.getProject(PROJECT_NAMES[0]);
		} else if (CHOSENPROJECTNAME != null) {
			project = FileToolBox.getProject(CHOSENPROJECTNAME);
		}
		if (projectDialogField != null) {
			projectDialogField.setDialogFieldListener(new IDialogFieldListener() {

				public void dialogFieldChanged(IDialogField field) {

				}

			});
			projectDialogField.setFocus();
			projectDialogField.setItems(PROJECT_NAMES);
			if (PROJECT_NAMES.length == 1) {
				projectDialogField.selectItem(0);
				project = FileToolBox.getProject(PROJECT_NAMES[0]);
			}
			if (CHOSENPROJECTNAME != null) {
				projectDialogField.selectItem(CHOSENPROJECTNAME);
				project = FileToolBox.getProject(CHOSENPROJECTNAME);
			}
			projectDialogField.setDialogFieldListener(getWizardAdapter());

		}

		SearchContext searchContext = new SearchContext();
		searchContext.setDealWithTransitive(AggregatedProperties.getDealWithTransitive(project));
		searchContext.setDealWithOptional(AggregatedProperties.getDealWithOptional(project));
		searchContext.setDealWithUnknownOrRestrictiveScope(AggregatedProperties.getDealWithNarrow(project));
		if (DependenciesHelper.resolvedArtifacts(searchContext, LIBRARIES)) {
			webAppDialogField.setElements(DependenciesHelper.filterResolvedForWebInfInWebProjects(searchContext, LIBRARIES));
			librariesDialogField.setElements(DependenciesHelper.filterResolvedForClasspathInWebProjects(searchContext, LIBRARIES));
		}
		if (DependenciesHelper.unresolvedLibraries(searchContext, LIBRARIES)) {
			issuesLibrariesDialogField.setElements(DependenciesHelper.filterUnresolved(searchContext, LIBRARIES));
		}
		if (DependenciesHelper.unknownOrRestrictedScope(searchContext, LIBRARIES)) {
			undeterminedLibrariesDialogField.setElements(DependenciesHelper.filterResolvedWithRiskyScope(searchContext, LIBRARIES));
			Boolean selected = AggregatedProperties.getAutomaticallyAddUnknown(project);
			keepUndeterminedDialogField.setSelection(selected.booleanValue());
		}
		if (DependenciesHelper.containsConflictingClasspathEntries(searchContext, LIBRARIES)) {
			conflictingLibrariesDialogField.setElements(DependenciesHelper.extractConflictingClasspathEntries(searchContext, LIBRARIES));
			Boolean selected = AggregatedProperties.getAutomaticallyRemoveConflicting(project);
			removeConflictingDialogField.setSelection(selected.booleanValue());
		}
		if (DependenciesHelper.transitiveDependencies(searchContext, LIBRARIES)) {
			transitiveDependenciesDialogField.setElements(new LinkedList(DependenciesHelper.extractTransitiveDependencies(searchContext, LIBRARIES)));
			Boolean selected = AggregatedProperties.getAutomaticallyAddTransitive(project);
			addTransitiveDependenciesField.setSelection(selected.booleanValue());
		}
	}

	/**
	 * Touch.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#touch()
	 */
	@Override
	protected void touch() {
		updateStatus(validate());
	}

	/**
	 * Validate.
	 * 
	 * @return the i status
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#validate()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected IStatus validate() {
		IStatus status = new StatusInfo();
		if (projectDialogField != null && projectDialogField.getSelectionIndex() < 0) {
			status = new StatusInfo(IStatus.ERROR, WizardsMessages.PomWebAppSynchronizationWizardPage_chooseProject);
		} else {
			String projectName = CHOSENPROJECTNAME;
			if (projectDialogField != null) {
				projectName = PROJECT_NAMES[projectDialogField.getSelectionIndex()];
			} else {
				projectName = getChosenProjectName();
			}
			setChosenProjectName(projectName);
			IProject project = FileToolBox.getProject(projectName);
			SearchContext searchContext = new SearchContext();
			searchContext.setDealWithTransitive(AggregatedProperties.getDealWithTransitive(project));
			searchContext.setDealWithOptional(AggregatedProperties.getDealWithOptional(project));
			searchContext.setDealWithUnknownOrRestrictiveScope(AggregatedProperties.getDealWithNarrow(project));
			if (getValidateLibraries() && DependenciesHelper.resolvedArtifacts(searchContext, LIBRARIES)) {
				if (DependenciesHelper.transitiveDependencies(searchContext, LIBRARIES)) {
					List libraries = librariesDialogField.getElements();
					libraries.addAll(webAppDialogField.getElements());
					setLibrariesOk(!DependenciesHelper.containsDuplicateLibraries(libraries, transitiveDependenciesDialogField.getElements()));
				} else {
					setLibrariesOk(true);
				}
			} else {
				setLibrariesOk(true);
			}
			if (!areLibrariesOk()) {
				status = new StatusInfo(IStatus.WARNING, WizardsMessages.PomWebAppSynchronizationWizardPage_retrieveDuplicates);
				setValidateLibraries(false);
			} else {
				if (DependenciesHelper.unresolvedTransitiveLibraries(searchContext, LIBRARIES)) {
					status = new StatusInfo(IStatus.WARNING, WizardsMessages.PomWebAppSynchronizationWizardPage_transitiveNotFound);
				} else {
					status = new StatusInfo(IStatus.INFO, WizardsMessages.PomWebAppSynchronizationWizardPage_info_projectTargetted);
				}
			}
		}
		return status;
	}

	/**
	 * Handle change control pressed.
	 * 
	 * @param field
	 *            the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleChangeControlPressed(org.org.eclipse.core.utils.platform.fields.IDialogField)
	 */
	@Override
	protected void handleChangeControlPressed(IDialogField field) {
		touch();
	}

	/**
	 * Handle custom button pressed.
	 * 
	 * @param field
	 *            the field
	 * @param buttonIndex
	 *            the button index
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleCustomButtonPressed(org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField, int)
	 */
	@Override
	protected void handleCustomButtonPressed(IListDialogField field, int buttonIndex) {
		touch();
	}

	/**
	 * Handle dialog field changed.
	 * 
	 * @param field
	 *            the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleDialogFieldChanged(org.org.eclipse.core.utils.platform.fields.IDialogField)
	 */
	@Override
	protected void handleDialogFieldChanged(IDialogField field) {
		touch();
		if (projectDialogField != null && field.equals(projectDialogField) && projectDialogField.getSelectionIndex() != -1) {
			getWizardContainer().getDisplay().asyncExec(new Runnable() {

				public void run() {
					reOpenWizardWithChangedProject();
				}

			}

			);
		}
	}

	/**
	 * Handle double clicked.
	 * 
	 * @param field
	 *            the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleDoubleClicked(org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField)
	 */
	@Override
	protected void handleDoubleClicked(IListDialogField field) {
		touch();
	}

	/**
	 * Handle selection changed.
	 * 
	 * @param field
	 *            the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleSelectionChanged(org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField)
	 */
	@Override
	protected void handleSelectionChanged(IListDialogField field) {
		touch();
	}

	/**
	 * Gets the classpath targetted libraries.
	 * 
	 * @return the classpath targetted libraries
	 */
	public Set<ResolvedArtifact> getClasspathTargettedLibraries() {
		return filterClasspathTargetted();
	}

	/**
	 * Gets the lib targetted libraries.
	 * 
	 * @return the lib targetted libraries
	 */
	public Set<ResolvedArtifact> getLibTargettedLibraries() {
		return filterLibTargetted();
	}

	/**
	 * Gets the conflicting classpath entries.
	 * 
	 * @return the conflicting classpath entries
	 */
	public Set<DWSClasspathEntryDescriptor> getConflictingClasspathEntries() {
		return filterConflictingClasspathEntries();
	}

	/**
	 * Filter classpath targetted.
	 * 
	 * @return the set< resolved artifact>
	 */
	private Set<ResolvedArtifact> filterClasspathTargetted() {
		Set<ResolvedArtifact> result = new HashSet<ResolvedArtifact>();
		if (librariesDialogField != null) {
			for (Object artifact : librariesDialogField.getElements()) {
				if (artifact instanceof ResolvedArtifact) {
					result.add((ResolvedArtifact) artifact);
				}
			}
		}
		if (keepUndeterminedDialogField != null && keepUndeterminedDialogField.isSelected()) {
			for (Object artifact : undeterminedLibrariesDialogField.getElements()) {
				if (artifact instanceof ResolvedArtifact) {
					result.add((ResolvedArtifact) artifact);
				}
			}
		}
		return result;

	}

	/**
	 * Filter lib targetted.
	 * 
	 * @return the set< resolved artifact>
	 */
	private Set<ResolvedArtifact> filterLibTargetted() {
		Set<ResolvedArtifact> result = new HashSet<ResolvedArtifact>();
		if (webAppDialogField != null) {
			for (Object artifact : webAppDialogField.getElements()) {
				if (artifact instanceof ResolvedArtifact) {
					result.add((ResolvedArtifact) artifact);
				}
			}
		}
		IProject project = FileToolBox.getProject(CHOSENPROJECTNAME);
		SearchContext searchContext = new SearchContext();
		searchContext.setDealWithTransitive(AggregatedProperties.getDealWithTransitive(project));
		searchContext.setDealWithOptional(AggregatedProperties.getDealWithOptional(project));
		searchContext.setDealWithUnknownOrRestrictiveScope(AggregatedProperties.getDealWithNarrow(project));
		if (DependenciesHelper.transitiveDependencies(searchContext, LIBRARIES) && (addTransitiveDependenciesField != null && addTransitiveDependenciesField.isSelected())) {
			for (Object artifact : transitiveDependenciesDialogField.getElements()) {
				if (artifact instanceof ResolvedArtifact) {
					result.add((ResolvedArtifact) artifact);
				}
			}
		}
		return result;
	}

	/**
	 * Filter conflicting classpath entries.
	 * 
	 * @return the set< dws classpath entry descriptor>
	 */
	private Set<DWSClasspathEntryDescriptor> filterConflictingClasspathEntries() {
		Set<DWSClasspathEntryDescriptor> result = new HashSet<DWSClasspathEntryDescriptor>();
		if (removeConflictingDialogField != null && removeConflictingDialogField.isSelected()) {
			for (Object classPathEntry : conflictingLibrariesDialogField.getElements()) {
				result.add((DWSClasspathEntryDescriptor) classPathEntry);
			}
		}
		return result;
	}

	/**
	 * Refresh library fields.
	 * 
	 * @see org.org.eclipse.dws.ui.internal.wizards.pages.AbstractPomSyncWizardPage#refreshLibraryFields()
	 */
	@Override
	public void refreshLibraryFields() {
		touch();
		librariesDialogField.refresh();
		webAppDialogField.refresh();
		if (conflictingLibrariesDialogField != null) {
			conflictingLibrariesDialogField.refresh();
		}
		if (undeterminedLibrariesDialogField != null) {
			undeterminedLibrariesDialogField.refresh();
		}
		if (transitiveDependenciesDialogField != null) {
			transitiveDependenciesDialogField.refresh();
		}
	}

	/**
	 * Re open wizard.
	 * 
	 * @see org.org.eclipse.dws.ui.internal.wizards.pages.AbstractPomSyncWizardPage#reOpenWizardWithSameProject()
	 */
	@Override
	protected void reOpenWizardWithSameProject() {
		PomDependenciesFilteringOptions filteringOptions = null;
		Pom pom = null;
		AbstractPomSynchronizationWizard wizard = null;
		if (getWizard() instanceof AbstractPomSynchronizationWizard) {
			filteringOptions = ((AbstractPomSynchronizationWizard) getWizard()).getFILTERING_OPTIONS();
			pom = ((AbstractPomSynchronizationWizard) getWizard()).getPARSED_POM_DESCRIPTION();
		}
		closeWizard();

		if (filteringOptions != null && pom != null) {
			wizard = new PomWebAppSynchronizationWizard(filteringOptions, pom);
		} else {
			wizard = new PomWebAppSynchronizationWizard(LIBRARIES);
		}
		WizardDialog wizardDialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		wizardDialog.open();
	}

	/**
	 * Re open wizard.
	 * 
	 * @see org.org.eclipse.dws.ui.internal.wizards.pages.AbstractPomSyncWizardPage#reOpenWizardWithSameProject()
	 */
	protected void reOpenWizardWithChangedProject() {
		PomDependenciesFilteringOptions filteringOptions = null;
		Pom pom = null;
		AbstractPomSynchronizationWizard wizard = null;
		if (getWizard() instanceof AbstractPomSynchronizationWizard) {
			filteringOptions = ((AbstractPomSynchronizationWizard) getWizard()).getFILTERING_OPTIONS();
			pom = ((AbstractPomSynchronizationWizard) getWizard()).getPARSED_POM_DESCRIPTION();
		}

		closeWizard();
		IProject project = PluginToolBox.getCurrentWorkspace().getRoot().getProject(getChosenProjectName());
		if (filteringOptions != null && pom != null) {
			String pomFileName = getPomFileName(project);
			IFile pomFile = (IFile) project.findMember(pomFileName);
			filteringOptions = PomInteractionHelper.preparePomDependenciesFilteringOptions(project);
			pom = null;
			try {
				pom = PomInteractionHelper.getParsedPomDescription(pomFile);
				pom.filterDependencies(filteringOptions);
			} catch (PomInteractionException e) {
				throw new WizardInitException(Status.ERROR, e);
			}
			wizard = new PomWebAppSynchronizationWizard(filteringOptions, pom);
			wizard.setCHOSENPROJECT(project.getName());
		} else {
			wizard = new PomWebAppSynchronizationWizard(LIBRARIES);
			wizard.setCHOSENPROJECT(project.getName());
		}
		WizardDialog wizardDialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		wizardDialog.open();
	}

	private String getPomFileName(IProject project) {
		String result = "pom.xml";
		String pomFileNames = AggregatedProperties.getPomFileNames(project);
		StringTokenizer tkz = new StringTokenizer(pomFileNames, ConfigurationConstants.POM_FILES_SEPARATOR, false);
		List<String> pomFileNamesList = new ArrayList<String>();
		while (tkz.hasMoreTokens()) {
			pomFileNamesList.add(tkz.nextToken());
		}
		if (pomFileNamesList.size() > 1) {
			ComboSelectionDialog dialog = new ComboSelectionDialog(getShell(), "Choose pom file", "Choose in the following list:", pomFileNamesList.toArray(new String[] {}), 0);
			dialog.open();
			result = dialog.getSelectedString();
		} else {
			result = pomFileNamesList.get(0);
		}
		return result;
	}
}
