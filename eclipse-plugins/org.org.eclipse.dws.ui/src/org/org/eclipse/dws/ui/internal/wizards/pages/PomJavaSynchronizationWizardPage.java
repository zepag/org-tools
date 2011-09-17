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
import org.org.eclipse.dws.ui.internal.wizards.PomJavaClasspathSynchronizationWizard;
import org.org.eclipse.dws.ui.internal.wizards.PomJavaFolderSynchronizationWizard;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException.Status;

/**
 * The Class PomJavaSynchronizationWizardPage.
 */
public class PomJavaSynchronizationWizardPage extends AbstractPomSyncWizardPage {

	/** The Constant WIZARD_PAGE_ID. */
	public static final String WIZARD_PAGE_ID = PomJavaSynchronizationWizardPage.class.getName();

	/** The Constant PREFERENCES_LINK_LABEL. */
	private static final String PREFERENCES_LINK_LABEL = WizardsMessages.PomJavaSynchronizationWizardPage_preferences_link;

	/** The project dialog field. */
	private ComboDialogField projectDialogField;

	/** The libraries dialog field. */
	private TreeListDialogField librariesDialogField;

	/** The keep undetermined dialog field. */
	private SelectionButtonDialogField keepUndeterminedDialogField;

	/** The undetermined libraries dialog field. */
	private TreeListDialogField undeterminedLibrariesDialogField;

	/** The label. */
	private Label label;

	/** The preferences link. */
	private Link preferencesLink;

	/**
	 * Instantiates a new pom java synchronization wizard page.
	 * 
	 * @param projectNames
	 *            the project names
	 * @param libraries
	 *            the libraries
	 * @param string
	 *            the string
	 */
	public PomJavaSynchronizationWizardPage(String[] projectNames, Set<AbstractChosenArtifactVersion> libraries, String string) {
		super(string, projectNames, libraries, WIZARD_PAGE_ID, WizardsMessages.PomJavaSynchronizationWizardPage_title, WizardsMessages.PomJavaSynchronizationWizardPage_description);
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
			label.setText(WizardsMessages.PomJavaSynchronizationWizardPage_using_project_preferences + project.getName());
		} else {
			Composite workspacePreferences = new Composite(getWizardContainer(), SWT.BORDER);
			workspacePreferences.setLayout(new GridLayout(2, false));
			label = new Label(workspacePreferences, SWT.FLAT);
			label.setText(WizardsMessages.PomJavaSynchronizationWizardPage_using_workspace_preferences);
			preferencesLink = new Link(workspacePreferences, SWT.NONE);
			preferencesLink.setFont(getWizardContainer().getFont());
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
		WizardContentsHelper.createExpandItem(expandBar, targetProjectComposite, WizardsMessages.PomJavaSynchronizationWizardPage_targetProject, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
		SearchContext searchContext = new SearchContext();
		searchContext.setDealWithTransitive(AggregatedProperties.getDealWithTransitive(project));
		searchContext.setDealWithOptional(AggregatedProperties.getDealWithOptional(project));
		searchContext.setDealWithUnknownOrRestrictiveScope(AggregatedProperties.getDealWithNarrow(project));
		if (DependenciesHelper.unresolvedLibraries(searchContext, LIBRARIES)) {
			Composite notFoundComposite = describeNotFoundSection(expandBar);
			notFoundComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			WizardContentsHelper.createExpandItem(expandBar, notFoundComposite, WizardsMessages.PomJavaSynchronizationWizardPage_librariesWithIssues, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_ERROR_16));
		}
		if (DependenciesHelper.resolvedArtifacts(searchContext, LIBRARIES)) {
			Composite addedLibrariesComposite = describeAddedLibrariesSection(expandBar);
			addedLibrariesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			ExpandItem librariesExpandItem = WizardContentsHelper.createExpandItem(expandBar, addedLibrariesComposite, WizardsMessages.PomJavaSynchronizationWizardPage_addedLibraries, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
			librariesExpandItem.setExpanded(false);
		}
		if (DependenciesHelper.transitiveDependencies(searchContext, LIBRARIES)) {
			Boolean automaticallyAddToClasspath = AggregatedProperties.getAutomaticallyAddTransitive(project);
			Composite transitiveDependenciesComposite = describeTransitiveDependenciesSection(expandBar, WizardsMessages.PomJavaSynchronizationWizardPage_addTransitiveLibrariesCheckbox);
			transitiveDependenciesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			ExpandItem transitiveDependenciesExpandItem = WizardContentsHelper.createExpandItem(expandBar, transitiveDependenciesComposite, WizardsMessages.PomJavaSynchronizationWizardPage_transitiveDependencies, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
			if (!automaticallyAddToClasspath) {
				transitiveDependenciesExpandItem.setImage(PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_WARNING_16));
			}
			transitiveDependenciesExpandItem.setExpanded(false);
		}
		if (DependenciesHelper.containsConflictingClasspathEntries(searchContext, LIBRARIES)) {
			Boolean automaticallyRemoveFromClasspath = AggregatedProperties.getAutomaticallyRemoveConflicting(project);
			Composite conflictingComposite = describeConflictingSection(expandBar);
			conflictingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			ExpandItem conflictingLibrariesExpandItem = WizardContentsHelper.createExpandItem(expandBar, conflictingComposite, WizardsMessages.PomJavaSynchronizationWizardPage_conflictingLibraries, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
			if (!automaticallyRemoveFromClasspath) {
				conflictingLibrariesExpandItem.setImage(PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_WARNING_16));
			}
			conflictingLibrariesExpandItem.setExpanded(false);
		}
		if (DependenciesHelper.unknownOrRestrictedScope(searchContext, LIBRARIES)) {
			Boolean automaticallyAddUnknown = AggregatedProperties.getAutomaticallyAddUnknown(project);
			Composite unknownComposite = describeUnknownSection(expandBar);
			unknownComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			ExpandItem undeterminedLibrariesExpandItem = WizardContentsHelper.createExpandItem(expandBar, unknownComposite, WizardsMessages.PomJavaSynchronizationWizardPage_unknownOrRestrictiveScope, PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_OK_16));
			if (!automaticallyAddUnknown) {
				undeterminedLibrariesExpandItem.setImage(PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.SMALL_WARNING_16));
			}
			undeterminedLibrariesExpandItem.setExpanded(false);
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
		keepUndeterminedDialogField.setLabelText(WizardsMessages.PomJavaSynchronizationWizardPage_addUnknownOrRestrictiveToClasspath);
		keepUndeterminedDialogField.setSelection(false);
		keepUndeterminedDialogField.setEnabled(true);
		keepUndeterminedDialogField.doFillIntoTable(composite, 1);
		undeterminedLibrariesDialogField = new TreeListDialogField(new UndeterminedListAdapter(), null, new LibrariesLabelProvider(this));
		undeterminedLibrariesDialogField.setLabelText(""); //$NON-NLS-1$
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
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		addTransitiveDependenciesField = new SelectionButtonDialogField(SWT.CHECK);
		addTransitiveDependenciesField.setLabelText(label);
		addTransitiveDependenciesField.setSelection(false);
		addTransitiveDependenciesField.setEnabled(true);
		addTransitiveDependenciesField.doFillIntoTable(composite, 3);
		transitiveDependenciesDialogField = new TreeListDialogField(new TransitiveListAdapter(this), null, new LibrariesLabelProvider(this));
		transitiveDependenciesDialogField.setLabelText(""); //$NON-NLS-1$
		transitiveDependenciesDialogField.setEnabled(true);
		transitiveDependenciesDialogField.setTreeExpansionLevel(10);
		transitiveDependenciesDialogField.doFillIntoTable(composite, 3);
		transitiveDependenciesDialogField.getTreeControl(null).setMenu(createTransitiveDependenciesMenu());
		return composite;
	}

	/**
	 * Describe added libraries section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describeAddedLibrariesSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		Label artifactSelection = new Label(composite, SWT.FLAT);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		artifactSelection.setLayoutData(data);
		artifactSelection.setText(WizardsMessages.PomJavaSynchronizationWizardPage_selectAndPressDEL);
		librariesDialogField = new TreeListDialogField(new LibrariesListAdapter(this), null, new LibrariesLabelProvider(this));
		librariesDialogField.setLabelText(""); //$NON-NLS-1$
		librariesDialogField.setEnabled(true);
		librariesDialogField.doFillIntoTable(composite, 3);
		librariesDialogField.getTreeControl(null).setMenu(createClasspathMenu());
		return composite;
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
		// if (project != null && project.exists()) {
		// Label label = new Label(composite, SWT.FLAT);
		// label.setText(project.getName());
		// label.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		// } else {
		projectDialogField = new ComboDialogField(SWT.READ_ONLY);
		projectDialogField.setLabelText(WizardsMessages.PomJavaSynchronizationWizardPage_chooseJavaProject);
		projectDialogField.setDialogFieldListener(getWizardAdapter());
		projectDialogField.doFillIntoTable(composite, 2);
		if (project != null && project.exists()) {
			projectDialogField.selectItem(project.getName());
		}
		// }
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
		menuItem.setText(WizardsMessages.PomJavaSynchronizationWizardPage_remove);
		menuItem.addSelectionListener(new SelectionListener() {
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
	 * Creates the transitive dependencies menu.
	 * 
	 * @return the menu
	 */
	private Menu createTransitiveDependenciesMenu() {
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(WizardsMessages.PomJavaSynchronizationWizardPage_remove);
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
	 * Initialize.
	 * 
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
		final Set<AbstractChosenArtifactVersion> resolvedArtifacts = DependenciesHelper.filterResolved(searchContext, LIBRARIES);
		if (DependenciesHelper.resolvedArtifacts(searchContext, LIBRARIES)) {
			librariesDialogField.setElements(new LinkedList(resolvedArtifacts));
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
	protected IStatus validate() {
		IStatus status = new StatusInfo();
		if (projectDialogField != null && projectDialogField.getSelectionIndex() < 0) {
			status = new StatusInfo(IStatus.ERROR, WizardsMessages.PomJavaSynchronizationWizardPage_choose_project);
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
				List<?> libraries = librariesDialogField.getElements();
				if (DependenciesHelper.transitiveDependencies(searchContext, LIBRARIES)) {
					List<?> transitiveDependencies = transitiveDependenciesDialogField.getElements();
					setLibrariesOk(!DependenciesHelper.containsDuplicateLibraries(libraries, transitiveDependencies));
				} else {
					setLibrariesOk(true);
				}
			} else {
				setLibrariesOk(true);
			}
			if (!areLibrariesOk()) {
				status = new StatusInfo(IStatus.WARNING, WizardsMessages.PomJavaSynchronizationWizardPage_warning_retrieve_duplicates);
				setValidateLibraries(false);
			} else {
				if (DependenciesHelper.unresolvedTransitiveLibraries(searchContext, LIBRARIES)) {
					status = new StatusInfo(IStatus.WARNING, WizardsMessages.PomJavaSynchronizationWizardPage_warning_transitive_not_found);
				} else {
					status = new StatusInfo(IStatus.INFO, WizardsMessages.PomJavaSynchronizationWizardPage_info_project_targetted);
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

				/**
				 * Run.
				 */
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
	 * Gets the selected libraries.
	 * 
	 * @return the selected libraries
	 */
	public Set<ResolvedArtifact> getSelectedLibraries() {
		return filterLibrariesForClasspath();
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
	 * Filter libraries for classpath.
	 * 
	 * @return the set< resolved artifact>
	 */
	private Set<ResolvedArtifact> filterLibrariesForClasspath() {
		Set<ResolvedArtifact> result = new HashSet<ResolvedArtifact>();
		if (librariesDialogField != null) {
			for (Object artifact : librariesDialogField.getElements()) {
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
		if (DependenciesHelper.unknownOrRestrictedScope(searchContext, LIBRARIES) && (keepUndeterminedDialogField != null && keepUndeterminedDialogField.isSelected())) {
			for (Object artifact : undeterminedLibrariesDialogField.getElements()) {
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
		if (conflictingLibrariesDialogField != null) {
			conflictingLibrariesDialogField.refresh();
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
		AbstractPomSynchronizationWizard wizard = null;
		PomDependenciesFilteringOptions filteringOptions = null;
		Pom pom = null;
		if (getWizard() instanceof AbstractPomSynchronizationWizard) {
			filteringOptions = ((AbstractPomSynchronizationWizard) getWizard()).getFILTERING_OPTIONS();
			pom = ((AbstractPomSynchronizationWizard) getWizard()).getPARSED_POM_DESCRIPTION();
			if (getWizard() instanceof PomJavaClasspathSynchronizationWizard) {
				if (filteringOptions != null && pom != null) {
					wizard = new PomJavaClasspathSynchronizationWizard(filteringOptions, pom);
				} else {
					wizard = new PomJavaClasspathSynchronizationWizard(LIBRARIES);
				}
			} else if (getWizard() instanceof PomJavaFolderSynchronizationWizard) {
				if (filteringOptions != null && pom != null) {
					wizard = new PomJavaFolderSynchronizationWizard(filteringOptions, pom);
				} else {
					wizard = new PomJavaFolderSynchronizationWizard(LIBRARIES);
				}
			}
		}
		closeWizard();
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
			if (getWizard() instanceof PomJavaClasspathSynchronizationWizard) {
				wizard = new PomJavaClasspathSynchronizationWizard(filteringOptions, pom);
			} else if (getWizard() instanceof PomJavaFolderSynchronizationWizard) {
				wizard = new PomJavaFolderSynchronizationWizard(filteringOptions, pom);
			}
			wizard.setCHOSENPROJECT(project.getName());
		} else {
			if (getWizard() instanceof PomJavaClasspathSynchronizationWizard) {
				wizard = new PomJavaClasspathSynchronizationWizard(LIBRARIES);
			} else if (getWizard() instanceof PomJavaFolderSynchronizationWizard) {
				wizard = new PomJavaFolderSynchronizationWizard(LIBRARIES);
			}
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
