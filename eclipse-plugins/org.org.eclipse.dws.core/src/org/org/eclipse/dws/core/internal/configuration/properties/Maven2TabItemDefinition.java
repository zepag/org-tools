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
package org.org.eclipse.dws.core.internal.configuration.properties;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.org.eclipse.core.utils.platform.properties.AbstractTabItemDefinition;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.configuration.preferences.GeneralPreferencePage;
import org.org.eclipse.dws.core.internal.images.PluginImages;

/**
 * The Class Maven2TabItemDefinition.
 * 
 * @author pagregoire
 */
public class Maven2TabItemDefinition extends AbstractTabItemDefinition {

	/** The Constant OPERATION_LABEL_ID. */
	public static final String OPERATION_LABEL_ID = PropertiesMessages.Maven2TabItemDefinition_id;

	/** The use preferences. */
	private Button usePreferences;

	/** The pom file names. */
	private Text pomFileNames;

	/** The web app folder. */
	private Text webAppFolder;

	/** The lib folder. */
	private Text libFolder;

	// private Button useContainer;

	// private Button useVariable;

	/** The variable name. */
	private Text variableName;

	/** The consider optional. */
	private Button considerOptional;

	/** The automatically remove conflicting. */
	private Button automaticallyRemoveConflicting;

	/** The deal with undetermined. */
	private Button dealWithUndetermined;

	/** The automatically add undetermined. */
	private Button automaticallyAddUndetermined;

	/** The deal with transitive. */
	private Button dealWithTransitive;

	/** The automatically add transitive. */
	private Button automaticallyAddTransitive;

	/** The preferences link. */
	private Link preferencesLink;

	/** The skipped dependency group id text. */
	private Text skippedDependencyGroupIdText;

	/** The skipped dependency artifact id text. */
	private Text skippedDependencyArtifactIdText;

	/** The add skipped dependency button. */
	private Button addSkippedDependencyButton;

	/** The add skipped dependency group. */
	private Group addSkippedDependencyGroup;

	/** The skipped dependencies table. */
	private Table skippedDependenciesTable;

	/** The clear skipped dependencies button. */
	private Button clearSkippedDependenciesButton;

	/** The clear skipped dependency button. */
	private Button clearSkippedDependencyButton;

	/** The pom file encoding. */
	private Text pomFileEncoding;

	/** The properties group. */
	private Group propertiesGroup;

	/** The property key text. */
	private Text propertyKeyText;

	/** The property value text. */
	private Text propertyValueText;

	/** The add property button. */
	private Button addPropertyButton;

	/** The clear properties button. */
	private Button clearPropertiesButton;

	/** The clear property button. */
	private Button clearPropertyButton;

	/** The properties table. */
	protected Table propertiesTable;

	/** The Constant TEXT_FIELD_WIDTH. */
	private static final int TEXT_FIELD_WIDTH = 50;

	/** The Constant USE_PREFERENCES_TITLE. */
	private static final String USE_PREFERENCES_TITLE = PropertiesMessages.Maven2TabItemDefinition_usePreferences;

	/** The Constant USE_PREFERENCES_TOOLTIP. */
	private static final String USE_PREFERENCES_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_usePreferencesDescription;

	/** The Constant POM_FILE_NAMES_TOOLTIP. */
	private static final String POM_FILE_NAMES_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_pomFileNamesDescription;

	/** The Constant POM_FILE_NAMES_TITLE. */
	private static final String POM_FILE_NAMES_TITLE = PropertiesMessages.Maven2TabItemDefinition_pomFileNames;

	/** The Constant WEB_APP_FOLDER_TOOLTIP. */
	private static final String WEB_APP_FOLDER_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_targetFolderDescription;

	/** The Constant WEB_APP_FOLDER_TITLE. */
	private static final String WEB_APP_FOLDER_TITLE = PropertiesMessages.Maven2TabItemDefinition_targetFolder;

	/** The Constant LIB_FOLDER_TITLE. */
	private static final String LIB_FOLDER_TITLE = PropertiesMessages.Maven2TabItemDefinition_libFolder;

	/** The Constant LIB_FOLDER_TOOLTIP. */
	private static final String LIB_FOLDER_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_libFolderDescription;

	/** The Constant CONSIDER_OPTIONAL_TOOLTIP. */
	private static final String CONSIDER_OPTIONAL_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_considerOptionalDescription;

	/** The Constant CONSIDER_OPTIONAL_TITLE. */
	private static final String CONSIDER_OPTIONAL_TITLE = PropertiesMessages.Maven2TabItemDefinition_considerOptional;

	/** The Constant AUTOMATICALLY_REMOVE_CONFLICTING_TITLE. */
	private static final String AUTOMATICALLY_REMOVE_CONFLICTING_TITLE = PropertiesMessages.Maven2TabItemDefinition_automaticallyRemoveConflictingDescription;

	/** The Constant AUTOMATICALLY_REMOVE_CONFLICTING_TOOLTIP. */
	private static final String AUTOMATICALLY_REMOVE_CONFLICTING_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_automaticallyRemoveConflicting;

	/** The Constant DEAL_WITH_UNDETERMINED_TITLE. */
	private static final String DEAL_WITH_UNDETERMINED_TITLE = PropertiesMessages.Maven2TabItemDefinition_dealWithUndetermined;

	/** The Constant DEAL_WITH_UNDETERMINED_TOOLTIP. */
	private static final String DEAL_WITH_UNDETERMINED_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_dealWithUndeterminedDescription;

	/** The Constant AUTOMATICALLY_ADD_UNDETERMINED_TOOLTIP. */
	private static final String AUTOMATICALLY_ADD_UNDETERMINED_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_automaticallyAddUndeterminedDescription;

	/** The Constant AUTOMATICALLY_ADD_UNDETERMINED_TITLE. */
	private static final String AUTOMATICALLY_ADD_UNDETERMINED_TITLE = PropertiesMessages.Maven2TabItemDefinition_automaticallyAddUndetermined;

	/** The Constant DEAL_WITH_TRANSITIVE_TOOLTIP. */
	private static final String DEAL_WITH_TRANSITIVE_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_dealWithTransitiveDescription;

	/** The Constant DEAL_WITH_TRANSITIVE_TITLE. */
	private static final String DEAL_WITH_TRANSITIVE_TITLE = PropertiesMessages.Maven2TabItemDefinition_dealWithTransitive;

	/** The Constant AUTOMATICALLY_ADD_TRANSITIVE_TOOLTIP. */
	private static final String AUTOMATICALLY_ADD_TRANSITIVE_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_automaticallyAddTransitive;

	/** The Constant AUTOMATICALLY_ADD_TRANSITIVE_TITLE. */
	private static final String AUTOMATICALLY_ADD_TRANSITIVE_TITLE = PropertiesMessages.Maven2TabItemDefinition_automaticallyAddTransitiveDescription;

	/** The Constant PREFERENCES_LINK_LABEL. */
	private static final String PREFERENCES_LINK_LABEL = PropertiesMessages.Maven2TabItemDefinition_configureWorkspaceSettings;

	// private static final String USE_CONTAINER_TOOLTIP = "Add libraries to a classpath container instead of putting them directly in the project's classpath";

	// private static final String USE_CONTAINER_TITLE = "U&se library container";

	// private static final String USE_VARIABLE_TITLE = "Us&e a MAVEN2_REPO variable";
	//
	// private static final String USE_VARIABLE_TOOLTIP = "Replace the raw path to the artifact download folder with a classpath variable. Useful for consistency when commiting to a source control manager..";

	/** The Constant VARIABLE_NAME_TITLE. */
	private static final String VARIABLE_NAME_TITLE = PropertiesMessages.Maven2TabItemDefinition_variableName;

	/** The Constant VARIABLE_NAME_TOOLTIP. */
	private static final String VARIABLE_NAME_TOOLTIP = PropertiesMessages.Maven2TabItemDefinition_variableNameDescription;

	/** The Constant POM_FILE_ENCODING_TITLE. */
	private static final String POM_FILE_ENCODING_TITLE = "Pom file encoding";

	/** The Constant POM_FILE_ENCODING_TOOLTIP. */
	private static final String POM_FILE_ENCODING_TOOLTIP = "Choose the Pom file's encoding.";

	/**
	 * Creates the contents.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @see org.org.eclipse.dws.utils.platform.properties.AbstractTabItemDefinition#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createContents(Composite parent) {

		final Composite fparent = parent;

		Composite usePrefsComposite = new Composite(parent, SWT.FLAT);
		usePrefsComposite.setLayout(new GridLayout(1, false));
		usePreferences = createLabelAndCheckBoxField(usePrefsComposite, USE_PREFERENCES_TITLE, USE_PREFERENCES_TOOLTIP, TEXT_FIELD_WIDTH);
		usePreferences.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				touch();
			}

			public void widgetSelected(SelectionEvent e) {
				touch();
			}
		});

		preferencesLink = new Link(usePrefsComposite, SWT.NONE);
		preferencesLink.setFont(usePrefsComposite.getFont());
		preferencesLink.setText("<A>" + PREFERENCES_LINK_LABEL + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$
		preferencesLink.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}

			private void doLinkActivated(Link link) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(fparent.getShell(), GeneralPreferencePage.class.getName(), null, null);
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}
		});

		TabFolder tabFolder = new TabFolder(parent, SWT.FLAT);
		Composite tabItem1content = new Composite(tabFolder, SWT.FLAT);
		tabItem1content.setLayout(new GridLayout(1, false));
		IProject project = (IProject) getElement().getAdapter(IProject.class);

		createGeneralTabContent(tabItem1content, fparent, project);

		Composite tabItem2content = new Composite(tabFolder, SWT.FLAT);
		tabItem2content.setLayout(new GridLayout(1, false));

		createAdvancedTabContent(tabItem2content);

		Composite tabItem3content = new Composite(tabFolder, SWT.FLAT);
		tabItem3content.setLayout(new GridLayout(1, false));

		createSkippedTabContent(tabItem3content);

		Composite tabItem4content = new Composite(tabFolder, SWT.FLAT);
		tabItem4content.setLayout(new GridLayout(1, false));

		createPropertiesTabContent(tabItem4content);

		TabItem tabItem1 = new TabItem(tabFolder, SWT.BORDER);
		tabItem1.setControl(tabItem1content);
		tabItem1.setText(PropertiesMessages.Maven2TabItemDefinition_basicSettingsTab);

		TabItem tabItem2 = new TabItem(tabFolder, SWT.BORDER);
		tabItem2.setControl(tabItem2content);
		tabItem2.setText(PropertiesMessages.Maven2TabItemDefinition_advancedSettingsTab);

		TabItem tabItem3 = new TabItem(tabFolder, SWT.BORDER);
		tabItem3.setControl(tabItem3content);
		tabItem3.setText("Skipped libraries");

		TabItem tabItem4 = new TabItem(tabFolder, SWT.BORDER);
		tabItem4.setControl(tabItem4content);
		tabItem4.setText("Pom Properties");

		PropertiesFacade.loadProperties(project);
		touch();
	}

	/**
	 * Creates the properties tab content.
	 * 
	 * @param tabItemcontent
	 *            the tab itemcontent
	 */
	private void createPropertiesTabContent(Composite tabItemcontent) {
		Composite inputComposite = new Composite(tabItemcontent, SWT.FLAT);
		inputComposite.setLayout(new GridLayout(1, false));
		inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label labelWidget = new Label(inputComposite, SWT.NONE);
		labelWidget.setText("Pom Properties");
		labelWidget.setToolTipText("Properties used in pom placeholders.");
		propertiesGroup = new Group(inputComposite, SWT.FLAT);
		propertiesGroup.setLayout(new GridLayout(2, false));
		propertiesGroup.setText("Add a property value");
		propertiesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Label keyLabel = new Label(propertiesGroup, SWT.NULL);
		keyLabel.setText("key");
		keyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		propertyKeyText = new Text(propertiesGroup, SWT.BORDER);
		propertyKeyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		propertyKeyText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				touch();
			}

			public void keyPressed(KeyEvent e) {
			}

		});
		Label valueLabel = new Label(propertiesGroup, SWT.NULL);
		valueLabel.setText("value");
		valueLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		propertyValueText = new Text(propertiesGroup, SWT.BORDER);
		propertyValueText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		propertyValueText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				touch();
			}

			public void keyPressed(KeyEvent e) {
			}

		});

		addPropertyButton = new Button(propertiesGroup, SWT.NULL);
		addPropertyButton.setText("Add property");
		addPropertyButton.setEnabled(false);
		addPropertyButton.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				addProperty();
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		Composite clearButtonsContainer = new Composite(inputComposite, SWT.NULL);
		clearButtonsContainer.setLayout(new GridLayout(2, false));
		clearPropertiesButton = new Button(clearButtonsContainer, SWT.NULL);
		clearPropertiesButton.setText("Clear all");
		clearPropertiesButton.setEnabled(false);
		clearPropertiesButton.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				removeProperties();
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		clearPropertyButton = new Button(clearButtonsContainer, SWT.NULL);
		clearPropertyButton.setText("Clear selected");
		clearPropertyButton.setEnabled(false);
		clearPropertyButton.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				removeProperties(propertiesTable.getSelectionIndices());
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		propertiesTable = new Table(inputComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		propertiesTable.setHeaderVisible(true);
		propertiesTable.setLinesVisible(true);
		propertiesTable.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				touch();
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		GridData data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
		data.heightHint = 100;
		propertiesTable.setLayoutData(data);
		TableColumn tc = new TableColumn(propertiesTable, SWT.LEFT);
		tc.setText("key");
		tc.setResizable(true);
		tc.setWidth(100);
		TableColumn tc2 = new TableColumn(propertiesTable, SWT.LEFT);
		tc2.setText("value");
		tc2.setResizable(true);
		tc2.setWidth(100);
	}

	/**
	 * Removes the properties.
	 * 
	 * @param selectionIndices
	 *            the selection indices
	 */
	protected void removeProperties(int[] selectionIndices) {
		propertiesTable.remove(selectionIndices);
		touch();

	}

	/**
	 * Removes the properties.
	 */
	protected void removeProperties() {
		propertiesTable.removeAll();
		touch();
	}

	/**
	 * Adds the property.
	 */
	protected void addProperty() {
		propertiesGroup.setEnabled(false);
		String key = propertyKeyText.getText();
		String value = propertyValueText.getText();
		propertyKeyText.setText(""); //$NON-NLS-1$
		propertyValueText.setText(""); //$NON-NLS-1$
		if (tableContains(key)) {
			removePropertyTableItem(key);
			createPropertyTableItem(key, value);
		} else {
			createPropertyTableItem(key, value);
		}
		touch();
		propertiesGroup.setEnabled(true);
	}

	/**
	 * Removes the property table item.
	 * 
	 * @param key
	 *            the key
	 */
	private void removePropertyTableItem(String key) {
		int index = -1;
		for (TableItem tableItem : propertiesTable.getItems()) {
			index++;
			if (tableItem.getText(0).equals(key)) {
				break;
			}
		}
		if (index >= 0) {
			propertiesTable.remove(index);
		}
	}

	/**
	 * Table contains.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return true, if successful
	 */
	private boolean tableContains(String key) {
		boolean result = false;
		for (TableItem tableItem : propertiesTable.getItems()) {
			if (tableItem.getText(0).equals(key)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Creates the property table item.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	private void createPropertyTableItem(String key, String value) {
		TableItem tableItem = new TableItem(propertiesTable, SWT.NONE, propertiesTable.getItemCount());
		tableItem.setText(new String[] { key, value });
	}

	/**
	 * Creates the skipped tab content.
	 * 
	 * @param tabItemcontent
	 *            the tab itemcontent
	 */
	private void createSkippedTabContent(Composite tabItemcontent) {
		Composite inputComposite = new Composite(tabItemcontent, SWT.FLAT);
		inputComposite.setLayout(new GridLayout(1, false));
		inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label labelWidget = new Label(inputComposite, SWT.NONE);
		labelWidget.setText(PropertiesMessages.Maven2TabItemDefinition_skippedDependencies);
		labelWidget.setToolTipText(PropertiesMessages.Maven2TabItemDefinition_skippedDependenciesDescription);
		addSkippedDependencyGroup = new Group(inputComposite, SWT.FLAT);
		addSkippedDependencyGroup.setLayout(new GridLayout(2, false));
		addSkippedDependencyGroup.setText(PropertiesMessages.Maven2TabItemDefinition_skippedDependenciesButtonAdd);
		addSkippedDependencyGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Label groupIdLabel = new Label(addSkippedDependencyGroup, SWT.NULL);
		groupIdLabel.setText(PropertiesMessages.Maven2TabItemDefinition_groupId);
		groupIdLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		skippedDependencyGroupIdText = new Text(addSkippedDependencyGroup, SWT.BORDER);
		skippedDependencyGroupIdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		skippedDependencyGroupIdText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				touch();
			}

			public void keyPressed(KeyEvent e) {
			}

		});
		Label artifactIdLabel = new Label(addSkippedDependencyGroup, SWT.NULL);
		artifactIdLabel.setText(PropertiesMessages.Maven2TabItemDefinition_artifactId);
		artifactIdLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		skippedDependencyArtifactIdText = new Text(addSkippedDependencyGroup, SWT.BORDER);
		skippedDependencyArtifactIdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		skippedDependencyArtifactIdText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				touch();
			}

			public void keyPressed(KeyEvent e) {
			}

		});

		addSkippedDependencyButton = new Button(addSkippedDependencyGroup, SWT.NULL);
		addSkippedDependencyButton.setText(PropertiesMessages.Maven2TabItemDefinition_add);
		addSkippedDependencyButton.setEnabled(false);
		addSkippedDependencyButton.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				addSkippedDependency();
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		Composite clearButtonsContainer = new Composite(inputComposite, SWT.NULL);
		clearButtonsContainer.setLayout(new GridLayout(2, false));
		clearSkippedDependenciesButton = new Button(clearButtonsContainer, SWT.NULL);
		clearSkippedDependenciesButton.setText(PropertiesMessages.Maven2TabItemDefinition_clearAll);
		clearSkippedDependenciesButton.setEnabled(false);
		clearSkippedDependenciesButton.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				removeDependencies();
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		clearSkippedDependencyButton = new Button(clearButtonsContainer, SWT.NULL);
		clearSkippedDependencyButton.setText(PropertiesMessages.Maven2TabItemDefinition_clearSelected);
		clearSkippedDependencyButton.setEnabled(false);
		clearSkippedDependencyButton.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				removeDependencies(skippedDependenciesTable.getSelectionIndices());
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		skippedDependenciesTable = new Table(inputComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		skippedDependenciesTable.setHeaderVisible(true);
		skippedDependenciesTable.setLinesVisible(true);
		skippedDependenciesTable.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				touch();
			}

			public void mouseDoubleClick(MouseEvent e) {
			}

		});
		GridData data = new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL);
		data.heightHint = 100;
		skippedDependenciesTable.setLayoutData(data);
		TableColumn tc = new TableColumn(skippedDependenciesTable, SWT.LEFT);
		tc.setText(PropertiesMessages.Maven2TabItemDefinition_group);
		tc.setResizable(true);
		tc.setWidth(100);
		TableColumn tc2 = new TableColumn(skippedDependenciesTable, SWT.LEFT);
		tc2.setText(PropertiesMessages.Maven2TabItemDefinition_artifact);
		tc2.setResizable(true);
		tc2.setWidth(100);
	}

	/**
	 * Creates the advanced tab content.
	 * 
	 * @param tabItemcontent
	 *            the tab itemcontent
	 */
	private void createAdvancedTabContent(Composite tabItemcontent) {
		considerOptional = createLabelAndCheckBoxField(tabItemcontent, CONSIDER_OPTIONAL_TITLE, CONSIDER_OPTIONAL_TOOLTIP, TEXT_FIELD_WIDTH);
		automaticallyRemoveConflicting = createLabelAndCheckBoxField(tabItemcontent, AUTOMATICALLY_REMOVE_CONFLICTING_TITLE, AUTOMATICALLY_REMOVE_CONFLICTING_TOOLTIP, TEXT_FIELD_WIDTH);
		dealWithUndetermined = createLabelAndCheckBoxField(tabItemcontent, DEAL_WITH_UNDETERMINED_TITLE, DEAL_WITH_UNDETERMINED_TOOLTIP, TEXT_FIELD_WIDTH);
		automaticallyAddUndetermined = createLabelAndCheckBoxField(tabItemcontent, AUTOMATICALLY_ADD_UNDETERMINED_TITLE, AUTOMATICALLY_ADD_UNDETERMINED_TOOLTIP, TEXT_FIELD_WIDTH);
		dealWithTransitive = createLabelAndCheckBoxField(tabItemcontent, DEAL_WITH_TRANSITIVE_TITLE, DEAL_WITH_TRANSITIVE_TOOLTIP, TEXT_FIELD_WIDTH);
		automaticallyAddTransitive = createLabelAndCheckBoxField(tabItemcontent, AUTOMATICALLY_ADD_TRANSITIVE_TITLE, AUTOMATICALLY_ADD_TRANSITIVE_TOOLTIP, TEXT_FIELD_WIDTH);
	}

	/**
	 * Creates the general tab content.
	 * 
	 * @param tabItemcontent
	 *            the tab itemcontent
	 * @param fparent
	 *            the fparent
	 * @param project
	 *            the project
	 */
	private void createGeneralTabContent(Composite tabItemcontent, final Composite fparent, IProject project) {
		pomFileNames = createLabelAndTextField(tabItemcontent, POM_FILE_NAMES_TITLE, POM_FILE_NAMES_TOOLTIP, TEXT_FIELD_WIDTH);
		pomFileNames.setText((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_POM_FILE_NAMES, false));
		pomFileEncoding = createLabelAndTextField(tabItemcontent, POM_FILE_ENCODING_TITLE, POM_FILE_ENCODING_TOOLTIP, TEXT_FIELD_WIDTH);
		pomFileEncoding.setText((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_POM_FILE_ENCODING, false));
		webAppFolder = createLabelAndTextField(tabItemcontent, WEB_APP_FOLDER_TITLE, WEB_APP_FOLDER_TOOLTIP, TEXT_FIELD_WIDTH);
		webAppFolder.setText((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_WEBAPP_FOLDER, false));
		libFolder = createLabelAndTextField(tabItemcontent, LIB_FOLDER_TITLE, LIB_FOLDER_TOOLTIP, TEXT_FIELD_WIDTH);
		libFolder.setText((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_FOLDER, false));
		// useContainer = createLabelAndCheckBoxField(tabItem1content, USE_CONTAINER_TITLE, USE_CONTAINER_TOOLTIP, TEXT_FIELD_WIDTH);
		// useVariable = createLabelAndCheckBoxField(tabItem1content, USE_VARIABLE_TITLE, USE_VARIABLE_TOOLTIP, TEXT_FIELD_WIDTH);
		variableName = createLabelAndTextField(tabItemcontent, VARIABLE_NAME_TITLE, VARIABLE_NAME_TOOLTIP, TEXT_FIELD_WIDTH);
		variableName.setText((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_VARIABLE_NAME, false));

	}

	/**
	 * Adds the skipped dependency.
	 */
	protected void addSkippedDependency() {
		addSkippedDependencyGroup.setEnabled(false);
		String groupId = skippedDependencyGroupIdText.getText();
		String artifactId = skippedDependencyArtifactIdText.getText();
		skippedDependencyGroupIdText.setText(""); //$NON-NLS-1$
		skippedDependencyArtifactIdText.setText(""); //$NON-NLS-1$
		createSkippedDependencyTableItem(groupId, artifactId);
		touch();
		addSkippedDependencyGroup.setEnabled(true);
	}

	/**
	 * Creates the skipped dependency table item.
	 * 
	 * @param groupId
	 *            the group id
	 * @param artifactId
	 *            the artifact id
	 */
	private void createSkippedDependencyTableItem(String groupId, String artifactId) {
		TableItem tableItem = new TableItem(skippedDependenciesTable, SWT.NONE, skippedDependenciesTable.getItemCount());
		tableItem.setText(new String[] { groupId, artifactId });
	}

	/**
	 * Removes the dependency.
	 * 
	 * @param index
	 *            the index
	 */
	protected void removeDependency(int index) {
		skippedDependenciesTable.remove(index);
		touch();
	}

	/**
	 * Removes the dependencies.
	 * 
	 * @param indices
	 *            the indices
	 */
	protected void removeDependencies(int[] indices) {
		skippedDependenciesTable.remove(indices);
		touch();
	}

	/**
	 * Removes the dependencies.
	 */
	protected void removeDependencies() {
		skippedDependenciesTable.removeAll();
		touch();
	}

	/**
	 * Touch.
	 */
	protected void touch() {
		if (skippedDependencyGroupIdText.getText().trim().equals("") || skippedDependencyArtifactIdText.getText().trim().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			addSkippedDependencyButton.setEnabled(false);
		} else {
			addSkippedDependencyButton.setEnabled(true);
		}
		if (skippedDependenciesTable.getItemCount() != 0) {
			clearSkippedDependenciesButton.setEnabled(true);
		} else {
			clearSkippedDependenciesButton.setEnabled(false);
		}

		if (skippedDependenciesTable.getSelectionCount() != 0) {
			clearSkippedDependencyButton.setEnabled(true);
		} else {
			clearSkippedDependencyButton.setEnabled(false);
		}

		if (propertyKeyText.getText().trim().equals("") || propertyValueText.getText().trim().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			addPropertyButton.setEnabled(false);
		} else {
			addPropertyButton.setEnabled(true);
		}
		if (propertiesTable.getItemCount() != 0) {
			clearPropertiesButton.setEnabled(true);
		} else {
			clearPropertiesButton.setEnabled(false);
		}

		if (propertiesTable.getSelectionCount() != 0) {
			clearPropertyButton.setEnabled(true);
		} else {
			clearPropertyButton.setEnabled(false);
		}

		if (!usePreferences.getSelection()) {
			enableAll();
		} else {
			disableAll();
		}
	}

	/**
	 * Disable all.
	 */
	protected void disableAll() {
		pomFileNames.setEnabled(false);
		pomFileEncoding.setEnabled(false);
		webAppFolder.setEnabled(false);
		libFolder.setEnabled(false);
		// useContainer.setEnabled(false);
		// useVariable.setEnabled(false);
		variableName.setEnabled(false);
		considerOptional.setEnabled(false);
		automaticallyRemoveConflicting.setEnabled(false);
		dealWithUndetermined.setEnabled(false);
		automaticallyAddUndetermined.setEnabled(false);
		dealWithTransitive.setEnabled(false);
		automaticallyAddTransitive.setEnabled(false);
		propertiesGroup.setEnabled(false);
		propertyKeyText.setEnabled(false);
		propertyValueText.setEnabled(false);
	}

	/**
	 * Enable all.
	 */
	protected void enableAll() {
		pomFileNames.setEnabled(true);
		pomFileEncoding.setEnabled(false);
		webAppFolder.setEnabled(true);
		libFolder.setEnabled(true);
		// useContainer.setEnabled(true);
		// useVariable.setEnabled(true);
		variableName.setEnabled(true);
		considerOptional.setEnabled(true);
		automaticallyRemoveConflicting.setEnabled(true);
		dealWithUndetermined.setEnabled(true);
		automaticallyAddUndetermined.setEnabled(true);
		dealWithTransitive.setEnabled(true);
		automaticallyAddTransitive.setEnabled(true);
		propertiesGroup.setEnabled(true);
		propertyKeyText.setEnabled(true);
		propertyValueText.setEnabled(true);
	}

	/**
	 * Creates the image.
	 * 
	 * @return the image
	 * 
	 * @see org.org.eclipse.dws.utils.platform.properties.AbstractTabItemDefinition#createImage()
	 */
	@Override
	protected Image createImage() {
		return DWSCorePlugin.getDefault().getImages().getImage(PluginImages.LOGO_MAVEN_SYNCHRONIZE_16);
	}

	/**
	 * Creates the title.
	 * 
	 * @return the string
	 * 
	 * @see org.org.eclipse.dws.utils.platform.properties.AbstractTabItemDefinition#createTitle()
	 */
	@Override
	protected String createTitle() {
		return PropertiesMessages.Maven2TabItemDefinition_title;
	}

	/**
	 * @see org.org.eclipse.core.utils.platform.properties.AbstractTabItemDefinition#storeProperties()
	 */
	@Override
	protected void storeProperties() throws CoreException {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_USE_WORSPACE_PREFERENCES, "" + usePreferences.getSelection()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, "" + automaticallyRemoveConflicting.getSelection()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, "" + considerOptional.getSelection()); //$NON-NLS-1$
		// PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_USE_LIBRARY_CONTAINER, "" + useContainer.getSelection());
		// PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_USE_VARIABLE, "" + useVariable.getSelection());
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_VARIABLE_NAME, "" + variableName.getText()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, "" + dealWithUndetermined.getSelection()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, "" + dealWithUndetermined.getSelection()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, "" + dealWithTransitive.getSelection()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_FOLDER, "" + libFolder.getText()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_POM_FILE_NAMES, "" + pomFileNames.getText()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_POM_FILE_ENCODING, "" + pomFileEncoding.getText()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, "" + automaticallyAddTransitive.getSelection()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, "" + automaticallyAddUndetermined.getSelection()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_WEBAPP_FOLDER, "" + webAppFolder.getText()); //$NON-NLS-1$
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES, getSkippedDependencies());
		PropertiesFacade.setProjectProperty(project.getProject(), PropertiesNames.P_MAVEN_PROJECT_POM_PROPERTIES, getPomProperties());
		PropertiesFacade.storePropertiesToFile(project.getProject());
	}

	/**
	 * Gets the pom properties.
	 * 
	 * @return the pom properties
	 */
	private String getPomProperties() {
		StringBuilder builder = new StringBuilder("|"); //$NON-NLS-1$
		for (TableItem tableItem : propertiesTable.getItems()) {
			builder.append(tableItem.getText(0) + "=" + tableItem.getText(1) + "|"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return builder.toString();
	}

	/**
	 * Gets the skipped dependencies.
	 * 
	 * @return the skipped dependencies
	 */
	private String getSkippedDependencies() {
		StringBuilder builder = new StringBuilder("|"); //$NON-NLS-1$
		for (TableItem tableItem : skippedDependenciesTable.getItems()) {
			builder.append(tableItem.getText(0) + "," + tableItem.getText(1) + "|"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return builder.toString();
	}

	/**
	 * Sets the skipped dependencies.
	 * 
	 * @param skippedDependenciesStr
	 *            the new skipped dependencies
	 */
	private void setSkippedDependencies(String skippedDependenciesStr) {
		StringTokenizer tkz = new StringTokenizer(skippedDependenciesStr, "|"); //$NON-NLS-1$
		while (tkz.hasMoreTokens()) {
			StringTokenizer tkz2 = new StringTokenizer(tkz.nextToken(), ","); //$NON-NLS-1$
			createSkippedDependencyTableItem(tkz2.nextToken(), tkz2.nextToken());
		}
	}

	/**
	 * @see org.org.eclipse.core.utils.platform.properties.AbstractTabItemDefinition#getProperties()
	 */
	@Override
	protected void getProperties() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		Boolean usePreferencesProperty = null;
		Boolean automaticallyRemoveConflictingProperty = null;
		Boolean considerOptionalProperty = null;
		Boolean useContainerProperty = null;
		// Boolean useVariableProperty = null;
		String variableNameProperty = null;
		Boolean dealWithUndeterminedProperty = null;
		Boolean dealWithTransitiveProperty = null;
		Boolean automaticallyAddTransitiveProperty = null;
		Boolean automaticallyAddUndeterminedProperty = null;
		String webAppFolderProperty = null;
		String libFolderProperty = null;
		String pomFileNamesProperty = null;
		String pomFileEncodingProperty = null;
		String skippedDependenciesProperty = null;
		String pomPropertiesProperty = null;
		try {
			usePreferencesProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_USE_WORSPACE_PREFERENCES, true));
			automaticallyRemoveConflictingProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE, true));
			considerOptionalProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES, true));
			useContainerProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_USE_LIBRARY_CONTAINER, true));
			// useVariableProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_USE_VARIABLE, true));
			variableNameProperty = (String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_VARIABLE_NAME, true);
			dealWithUndeterminedProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE, true));
			dealWithTransitiveProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES, true));
			automaticallyAddTransitiveProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD, true));
			automaticallyAddUndeterminedProperty = Boolean.valueOf((String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD, true));
			webAppFolderProperty = (String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_WEBAPP_FOLDER, true);
			libFolderProperty = (String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_FOLDER, true);
			pomFileNamesProperty = (String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_POM_FILE_NAMES, true);
			pomFileEncodingProperty = (String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_POM_FILE_ENCODING, true);
			skippedDependenciesProperty = (String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES, true);
			pomPropertiesProperty = (String) PropertiesFacade.getProjectProperty(project, PropertiesNames.P_MAVEN_PROJECT_SKIPPED_DEPENDENCIES, true);
		} catch (NullPointerException e) {
			usePreferencesProperty = null;
			automaticallyRemoveConflictingProperty = null;
			considerOptionalProperty = null;
			useContainerProperty = null;
			// useVariableProperty = null;
			variableNameProperty = null;
			dealWithUndeterminedProperty = null;
			dealWithTransitiveProperty = null;
			automaticallyAddTransitiveProperty = null;
			automaticallyAddUndeterminedProperty = null;
			webAppFolderProperty = null;
			libFolderProperty = null;
			pomFileNamesProperty = null;
			pomFileEncodingProperty = null;
			skippedDependenciesProperty = null;
			pomPropertiesProperty = null;
		}

		if (usePreferencesProperty == null || automaticallyRemoveConflictingProperty == null || considerOptionalProperty == null || dealWithUndeterminedProperty == null || dealWithTransitiveProperty == null || automaticallyAddTransitiveProperty == null || automaticallyAddUndeterminedProperty == null || webAppFolderProperty == null || libFolderProperty == null || pomFileNamesProperty == null || skippedDependenciesProperty == null || useContainerProperty == null || /* useVariableProperty == null || */variableNameProperty == null || pomPropertiesProperty == null) {
			performDefaults();
		} else {
			usePreferences.setSelection(usePreferencesProperty);
			automaticallyRemoveConflicting.setSelection(automaticallyRemoveConflictingProperty);
			considerOptional.setSelection(considerOptionalProperty);
			// useContainer.setSelection(useContainerProperty);
			// useVariable.setSelection(useVariableProperty);
			variableName.setText(variableNameProperty);
			dealWithUndetermined.setSelection(dealWithUndeterminedProperty);
			dealWithTransitive.setSelection(dealWithTransitiveProperty);
			automaticallyAddTransitive.setSelection(automaticallyAddTransitiveProperty);
			automaticallyAddUndetermined.setSelection(automaticallyAddUndeterminedProperty);
			webAppFolder.setText(webAppFolderProperty);
			libFolder.setText(libFolderProperty);
			pomFileNames.setText(pomFileNamesProperty);
			pomFileEncoding.setText(pomFileEncodingProperty);
			setSkippedDependencies(skippedDependenciesProperty);
			setPomProperties(pomPropertiesProperty);
		}
		if (usePreferences.getSelection()) {
			disableAll();
		} else {
			enableAll();
		}
	}

	/**
	 * Sets the pom properties.
	 * 
	 * @param pomPropertiesProperty
	 *            the new pom properties
	 */
	private void setPomProperties(String pomPropertiesProperty) {
		StringTokenizer tkz = new StringTokenizer(pomPropertiesProperty, "|", false); //$NON-NLS-1$
		while (tkz.hasMoreTokens()) {
			StringTokenizer tkz2 = new StringTokenizer(tkz.nextToken(), "=", false); //$NON-NLS-1$
			createPropertyTableItem(tkz2.nextToken(), tkz2.nextToken());
		}
	}

	/**
	 * Sets the defaults.
	 * 
	 * @see org.org.eclipse.dws.utils.platform.properties.AbstractTabItemDefinition#setDefaults()
	 */
	@Override
	protected void setDefaults() {
		PropertiesFacade.initDefaults();
		usePreferences.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_USE_WORSPACE_PREFERENCES));
		automaticallyRemoveConflicting.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_CONFLICTING_AUTOMATICALLY_REMOVE));
		considerOptional.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_CONSIDER_OPTIONAL_LIBRARIES));
		// useContainer.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_USE_LIBRARY_CONTAINER));
		// useVariable.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_USE_VARIABLE));
		variableName.setText((String) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_VARIABLE_NAME));
		dealWithUndetermined.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_DEAL_WITH_DEPENDENCIES_OF_UNDETERMINED_OR_RESTRICTIVE_SCOPE));
		dealWithTransitive.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_DEAL_WITH_TRANSITIVE_DEPENDENCIES));
		automaticallyAddTransitive.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_TRANSITIVE_DEPENDENCIES_AUTOMATICALLY_ADD));
		automaticallyAddUndetermined.setSelection((Boolean) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_UNDETERMINED_OR_RESTRICTIVE_AUTOMATICALLY_ADD));
		webAppFolder.setText((String) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_WEBAPP_FOLDER));
		libFolder.setText((String) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_FOLDER));
		pomFileNames.setText((String) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_POM_FILE_NAMES));
		pomFileEncoding.setText((String) PropertiesFacade.getDefaultPropertyValue(PropertiesNames.P_MAVEN_POM_FILE_ENCODING));
		setSkippedDependencies(""); //$NON-NLS-1$
		touch();
	}

	/**
	 * Inits the.
	 * 
	 * @see org.org.eclipse.dws.utils.platform.properties.AbstractTabItemDefinition#init()
	 */
	@Override
	protected void init() {
	}
}