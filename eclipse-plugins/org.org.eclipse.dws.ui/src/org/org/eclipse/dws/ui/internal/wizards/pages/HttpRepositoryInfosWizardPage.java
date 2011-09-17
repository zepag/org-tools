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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.ui.dialogs.InfoDialog;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.ComboDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogField;
import org.org.eclipse.core.utils.platform.fields.StringDialogField;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.core.utils.platform.wizards.page.WizardContentsHelper;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.eclipse.dws.ui.internal.wizards.NewRepositoryWizard;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.repository.crawler.items.IHttpCrawledRepositorySetup;
import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.immutable.ImmutablePatternSet;
import org.org.repository.crawler.items.mutable.AbstractCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.HttpCrawledRepositorySetup;
import org.org.repository.crawler.items.mutable.PatternSet;
import org.org.repository.crawler.mapping.Entry;
import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;

/**
 * The Class HttpRepositoryInfosWizardPage.
 * 
 * @author pagregoire
 */

public class HttpRepositoryInfosWizardPage extends AbstractRepositoryInfosWizardPage<IHttpCrawledRepositorySetup> {

	/**
	 * The Class ScanForEntriesRunnableWithProgress.
	 */
	private final class ScanForEntriesRunnableWithProgress implements IRunnableWithProgress {

		/** The http repository browser plugin. */
		private HttpRepositoryBrowserPlugin httpRepositoryBrowserPlugin;

		/**
		 * Run.
		 * 
		 * @param monitor
		 *            the monitor
		 * 
		 * @throws InvocationTargetException
		 *             the invocation target exception
		 * @throws InterruptedException
		 *             the interrupted exception
		 * 
		 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Scanning for entries", 1);
			httpRepositoryBrowserPlugin = prepareTestPlugin();
			try {
				Queue<Entry> entries = httpRepositoryBrowserPlugin.getEntryList(urlDialogField.getText());
				if (entries.size() == 0 || !containFolder(entries)) {
					status = new StatusInfo(IStatus.WARNING, "There seems to be no available entries in this repository location.\nParsing patterns could be wrong or the url may not point to a Maven 2 repository's root.");
				}
			} catch (IOException e) {
				logger.debug(e);
			}
			monitor.done();
		}

		/**
		 * Contain folder.
		 * 
		 * @param entries
		 *            the entries
		 * 
		 * @return true, if successful
		 */
		private boolean containFolder(Queue<Entry> entries) {
			boolean result = false;
			for (Entry entry : entries) {
				if (entry.getRawType() == Entry.RawType.DIRECTORY) {
					result = true;
					break;
				}
			}
			return result;
		}

		/**
		 * Gets the http repository plugin.
		 * 
		 * @return the http repository plugin
		 */
		public HttpRepositoryBrowserPlugin getHttpRepositoryPlugin() {
			return httpRepositoryBrowserPlugin;
		}
	}

	/** The Constant CONNECTION_OK. */
	private static final int CONNECTION_OK = 1 << 0;

	/** The Constant RESPONSE_STATE_NOT_200. */
	private static final int RESPONSE_STATE_NOT_200 = 1 << 1;

	/** The Constant CANNOT_REACH_URL. */
	private static final int CANNOT_REACH_URL = 1 << 2;

	/** The Constant URL_NOT_CORRECT. */
	private static final int URL_NOT_CORRECT = 1 << 3;

	/** Logger for this class. */
	private static Logger logger = Logger.getLogger(HttpRepositoryInfosWizardPage.class);

	/** The name dialog field. */
	private StringDialogField nameDialogField;

	/** The url dialog field. */
	private StringDialogField urlDialogField;

	/** The pattern set field. */
	private ComboDialogField patternSetField;

	/** The entry pattern dialog field. */
	private StringDialogField entryPatternDialogField;

	/** The parent pattern dialog field. */
	private StringDialogField parentPatternDialogField;

	/** The directory entry pattern dialog field. */
	private StringDialogField directoryEntryPatternDialogField;

	/** The file entry pattern dialog field. */
	private StringDialogField fileEntryPatternDialogField;
	//
	// /** The proxy host dialog field. */
	// private StringDialogField proxyHostDialogField;
	//
	// /** The proxy port dialog field. */
	// private StringDialogField proxyPortDialogField;

	/** The url validated. */
	private boolean urlValidated;

	/** The url last length. */
	private int urlLastLength = 0;

	/** The status. */
	private IStatus status;

	/** The validate button. */
	private Button validateButton;

	/** The list root entries button. */
	private Button listRootEntriesButton;

	private String proxyHost;

	private Integer proxyPort;

	private Link preferencesLink;

	/**
	 * Instantiates a new http repository infos wizard page.
	 */
	public HttpRepositoryInfosWizardPage() {
		super(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_id, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_title, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_description);
		setColumnsNumber(1);
	}

	/**
	 * Instantiates a new http repository infos wizard page.
	 * 
	 * @param label
	 *            the label
	 * @param repository
	 *            the repository
	 */
	public HttpRepositoryInfosWizardPage(String label, IHttpCrawledRepositorySetup repository) {
		this();
		this.label = label;
		this.repositorySetup = repository;
	}

	/**
	 * Describe.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#describe()
	 */
	@Override
	protected void describe() {
		final TabFolder tabFolder = new TabFolder(getWizardContainer(), SWT.NONE);
		tabFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite composite = WizardContentsHelper.createClientComposite(tabFolder);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		Composite repositoryInfoComposite = describeRepositoryInfoSection(composite);
		repositoryInfoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite filtersComposite = describeGroupFiltersSection(composite);
		filtersComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TabItem item = new TabItem(tabFolder, SWT.NONE);
		item.setText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_repository_infos);
		item.setControl(composite);

		Composite patternsComposite = describePatternsSection(tabFolder);
		filtersComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem item3 = new TabItem(tabFolder, SWT.NONE);
		item3.setText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_parsing_expandItem);
		item3.setControl(patternsComposite);

		Composite proxyInfoComposite = describeProxyInfoSection(tabFolder);
		proxyInfoComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem item4 = new TabItem(tabFolder, SWT.NONE);
		item4.setText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_proxy_expandItem);
		item4.setControl(proxyInfoComposite);

		validateButton = new Button(getWizardContainer(), SWT.NONE);
		validateButton.setText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_validate_url_button);
		validateButton.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
				try {
					validateUrl();
				} catch (Exception ex) {
					ErrorDialog errorDialog = new ErrorDialog(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_error_impossible_validate, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_error_impossible_validate_url, ex);
					errorDialog.open();
				}
				touch();
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		validateButton.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				if (e.getSource().equals(validateButton)) {
					try {
						validateUrl();
					} catch (Exception ex) {
						ErrorDialog errorDialog = new ErrorDialog(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_error_impossible_validate, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_error_impossible_validate_url, ex);
						errorDialog.open();
					}
					touch();
				}
			}

			public void keyPressed(KeyEvent e) {
			}

		});

	}

	/**
	 * Format entries.
	 * 
	 * @param entries
	 *            the entries
	 * 
	 * @return the string
	 */
	private String formatEntries(Queue<Entry> entries) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry entry : entries) {
			stringBuilder.append(entry.getRawType().name() + " : " + entry.getResolvedName() + "\n");
		}
		return stringBuilder.toString();
	}

	/**
	 * Describe proxy info section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describeProxyInfoSection(Composite parent) {
		Composite proxyInfoComposite = WizardContentsHelper.createClientComposite(parent);
		proxyInfoComposite.setLayout(new GridLayout(2, false));
		Label label = WizardContentsHelper.createDescriptionLabel(proxyInfoComposite, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_proxy_description);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		preferencesLink = new Link(proxyInfoComposite, SWT.NONE);
		preferencesLink.setFont(getWizardContainer().getFont());
		preferencesLink.setText("<A>" + "Configure the proxy through Eclipse preferences" + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$
		preferencesLink.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}

			private void doLinkActivated(Link link) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), "org.eclipse.ui.net.NetPreferences", null, null);
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				doLinkActivated((Link) e.widget);
			}
		});
		return proxyInfoComposite;
	}

	/**
	 * Describe patterns section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describePatternsSection(Composite parent) {
		Composite patternsComposite = WizardContentsHelper.createClientComposite(parent);
		patternsComposite.setLayout(new GridLayout(2, false));
		Label label = WizardContentsHelper.createDescriptionLabel(patternsComposite, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_parsing_description);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		patternSetField = new ComboDialogField(SWT.SIMPLE);
		// patternSetField.setItems(new String[] { HttpRepositoryBrowserPlugin.APACHE2_PATTERNSET.getLabel(), HttpRepositoryBrowserPlugin.TOMCAT6_PATTERNSET.getLabel(), HttpRepositoryBrowserPlugin.ARTIFACTORY_PATTERNSET.getLabel() });
		patternSetField.setItems(DWSCorePlugin.getDefault().getPatternSetLabels());
		patternSetField.setLabelText("Pattern set:");
		patternSetField.setDialogFieldListener(getWizardAdapter());
		patternSetField.doFillIntoTable(patternsComposite, 2);
		entryPatternDialogField = new StringDialogField();
		entryPatternDialogField.setLabelText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_entryPattern);
		entryPatternDialogField.setDialogFieldListener(getWizardAdapter());
		entryPatternDialogField.doFillIntoTable(patternsComposite, 2);
		entryPatternDialogField.setContentProposals(RepositoryModelPersistence.getEntryPatternAutocompleteProposals());
		parentPatternDialogField = new StringDialogField();
		parentPatternDialogField.setLabelText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_parentPattern);
		parentPatternDialogField.setDialogFieldListener(getWizardAdapter());
		parentPatternDialogField.doFillIntoTable(patternsComposite, 2);
		parentPatternDialogField.setContentProposals(RepositoryModelPersistence.getParentPatternAutocompleteProposals());
		directoryEntryPatternDialogField = new StringDialogField();
		directoryEntryPatternDialogField.setLabelText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_directoryEntryPattern);
		directoryEntryPatternDialogField.setDialogFieldListener(getWizardAdapter());
		directoryEntryPatternDialogField.doFillIntoTable(patternsComposite, 2);
		directoryEntryPatternDialogField.setContentProposals(RepositoryModelPersistence.getDirectoryEntryPatternAutocompleteProposals());
		fileEntryPatternDialogField = new StringDialogField();
		fileEntryPatternDialogField.setLabelText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_fileEntryPattern);
		fileEntryPatternDialogField.setDialogFieldListener(getWizardAdapter());
		fileEntryPatternDialogField.doFillIntoTable(patternsComposite, 2);
		fileEntryPatternDialogField.setContentProposals(RepositoryModelPersistence.getFileEntryPatternAutocompleteProposals());

		listRootEntriesButton = new Button(patternsComposite, SWT.NONE);
		listRootEntriesButton.setText("Test retrieval of root entries");
		listRootEntriesButton.addMouseListener(new MouseListener() {

			/**
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseUp(MouseEvent e) {
				if (e.getSource().equals(listRootEntriesButton)) {
					HttpRepositoryBrowserPlugin httpRepositoryBrowserPlugin = prepareTestPlugin();
					try {
						Queue<Entry> entries = httpRepositoryBrowserPlugin.getEntryList(urlDialogField.getText());
						InfoDialog infoDialog = new InfoDialog("Root entries for repository " + nameDialogField.getText(), "Found " + entries.size() + (entries.size() > 1 ? " entries." : " entry.") + "\n Open details in order to see the list.", formatEntries(entries));
						infoDialog.open();
					} catch (IOException ex) {
						ErrorDialog errorDialog = new ErrorDialog("Impossible to achieve", "Impossible to retrieve root entries", ex);
						errorDialog.open();
						logger.debug(ex);
					}
				}
			}

			/**
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent e) {
			}

			/**
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		listRootEntriesButton.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				if (e.getSource().equals(listRootEntriesButton)) {
					HttpRepositoryBrowserPlugin httpRepositoryBrowserPlugin = prepareTestPlugin();
					try {
						Queue<Entry> entries = httpRepositoryBrowserPlugin.getEntryList(urlDialogField.getText());
						InfoDialog infoDialog = new InfoDialog("Root entries for repository " + nameDialogField.getText(), "Found " + entries.size() + (entries.size() > 1 ? " entries." : " entry.") + "\n Open details in order to see the list.", formatEntries(entries));
						infoDialog.open();
					} catch (IOException ex) {
						logger.debug(ex);
					}
				}
			}

			public void keyPressed(KeyEvent e) {
			}

		});
		return patternsComposite;
	}

	/**
	 * Describe repository info section.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the composite
	 */
	private Composite describeRepositoryInfoSection(Composite parent) {
		Composite composite = WizardContentsHelper.createClientComposite(parent);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		nameDialogField = new StringDialogField();
		nameDialogField.setLabelText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_repositoryName);
		nameDialogField.setDialogFieldListener(getWizardAdapter());
		nameDialogField.doFillIntoTable(composite, 2);
		nameDialogField.setContentProposals(RepositoryModelPersistence.getRepositoryNameAutocompleteProposals());
		urlDialogField = new StringDialogField();
		urlDialogField.setLabelText(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_repositoryUrl);
		urlDialogField.setDialogFieldListener(getWizardAdapter());
		urlDialogField.doFillIntoTable(composite, 2);
		urlDialogField.setContentProposals(RepositoryModelPersistence.getHttpBrowsedRepositoryAutocompleteProposals());
		return composite;
	}

	/**
	 * Initialize.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#initialize()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void initialize() {
		nameDialogField.setFocus();
		if (getWizard() instanceof NewRepositoryWizard) {
			NewRepositoryWizard newRepositoryWizard = (NewRepositoryWizard) getWizard();
			IHttpCrawledRepositorySetup chosenSetup = (IHttpCrawledRepositorySetup) newRepositoryWizard.getChosenRepositorySetup();
			if (chosenSetup != null) {
				this.repositorySetup = chosenSetup;
				this.label = chosenSetup.getId();
			}
		}
		if (label != null) {
			nameDialogField.setText(label);
		}
		if (repositorySetup != null) {
			urlDialogField.setText(repositorySetup.getBaseUrl());
			if (repositorySetup.getPatternSet() != null) {
				if (repositorySetup.getPatternSet().getEntryPattern() != null) {
					entryPatternDialogField.setText(repositorySetup.getPatternSet().getEntryPattern());
				}
				if (repositorySetup.getPatternSet().getParentDirectoryPattern() != null) {
					parentPatternDialogField.setText(repositorySetup.getPatternSet().getParentDirectoryPattern());
				}
				if (repositorySetup.getPatternSet().getDirectoryEntryPattern() != null) {
					directoryEntryPatternDialogField.setText(repositorySetup.getPatternSet().getDirectoryEntryPattern());
				}
				if (repositorySetup.getPatternSet().getFileEntryPattern() != null) {
					fileEntryPatternDialogField.setText(repositorySetup.getPatternSet().getFileEntryPattern());
				}
			}
			if (repositorySetup.getGroupFilters().size() != 0) {
				setGroupFilters(new HashSet<String>(repositorySetup.getGroupFilters()));
				getGroupFiltersDialogField().addElements(new ArrayList(getGroupFilters()));
			}
		} else {
			getGroupFilters().add(AbstractCrawledRepositorySetup.KEEP_ALL_PATTERN);
			getGroupFiltersDialogField().addElement(AbstractCrawledRepositorySetup.KEEP_ALL_PATTERN);
			IPatternSet patternSet = RepositoryModelPersistence.getPatternSets();
			patternSetField.selectItem(patternSet.getLabel());
			mapPatternSet(patternSet);
		}
	}

	/**
	 * Map pattern set.
	 * 
	 * @param patternSet
	 *            the pattern set
	 */
	private void mapPatternSet(IPatternSet patternSet) {
		entryPatternDialogField.setText(patternSet.getEntryPattern());
		parentPatternDialogField.setText(patternSet.getParentDirectoryPattern());
		directoryEntryPatternDialogField.setText(patternSet.getDirectoryEntryPattern());
		fileEntryPatternDialogField.setText(patternSet.getFileEntryPattern());
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
	 * Validate url.
	 * 
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private void validateUrl() throws InvocationTargetException, InterruptedException {

		this.getContainer().run(true, true, new IRunnableWithProgress() {

			/**
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Testing URL", 1);
				int URL_STATUS = testURLConnection(urlDialogField.getText());
				if (URL_STATUS != CONNECTION_OK) {
					StringBuilder detail = new StringBuilder();
					switch (URL_STATUS) {
					case RESPONSE_STATE_NOT_200:
						detail.append(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_error_invalidurl1);
						break;
					case CANNOT_REACH_URL:
						detail.append(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_error_invalidurl2);
						break;
					case URL_NOT_CORRECT:
						detail.append(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_error_invalidurl3);
						break;
					default:
						detail.append(WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_error_invalidurl4);
						break;
					}
					status = new StatusInfo(IStatus.ERROR, detail.toString());
				} else {
					status = null;
				}
				urlValidated = true;
			}
		});
		ScanForEntriesRunnableWithProgress scanForEntriesRunnableWithProgress = new ScanForEntriesRunnableWithProgress();
		this.getContainer().run(false, false, scanForEntriesRunnableWithProgress);
		Proxy proxy = scanForEntriesRunnableWithProgress.getHttpRepositoryPlugin().getProxy();
		if (proxy != null && proxy.type() == Proxy.Type.HTTP && proxy.address() instanceof InetSocketAddress) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
			proxyHost = inetSocketAddress.getHostName();
			proxyPort = inetSocketAddress.getPort();
		}
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
		validateButton.setEnabled(false);
		if (nameDialogField == null || nameDialogField.getText().trim().equals("")) { //$NON-NLS-1$
			status = new StatusInfo(IStatus.ERROR, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_inputName);
		} else if (urlDialogField == null || urlDialogField.getText().trim().equals("")) { //$NON-NLS-1$
			status = new StatusInfo(IStatus.ERROR, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_inputUrl);
		} else {
			if (!urlValidated) {
				status = new StatusInfo(IStatus.ERROR, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_validateurl);
				validateButton.setEnabled(true);
			} else if (urlValidated && urlLastLength != urlDialogField.getText().length()) {
				urlValidated = false;
				status = new StatusInfo(IStatus.ERROR, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_validateurl);
				validateButton.setEnabled(true);
			} else if (urlValidated && this.status != null) {
				status = this.status;
				urlValidated = false;
			} else {
				status = new StatusInfo(IStatus.INFO, WizardsMessages.HttpBrowsedRepositoryInfosWizardPage_info_startbrowsing);
				HttpCrawledRepositorySetup httpCrawledRepositorySetup = new HttpCrawledRepositorySetup(urlDialogField.getText());
				PatternSet patternSet = new PatternSet();
				patternSet.setEntryPattern(entryPatternDialogField.getText());
				patternSet.setParentDirectoryPattern(parentPatternDialogField.getText());
				patternSet.setDirectoryEntryPattern(directoryEntryPatternDialogField.getText());
				patternSet.setFileEntryPattern(fileEntryPatternDialogField.getText());
				httpCrawledRepositorySetup.setPatternSet(patternSet.getImmutable());
				if (!(proxyHost == null)) { //$NON-NLS-1$
					httpCrawledRepositorySetup.setProxyHost(proxyHost);
				}
				if (!(proxyPort == null)) { //$NON-NLS-1$
					httpCrawledRepositorySetup.setProxyPort(proxyPort);
				}
				label = nameDialogField.getText();
				repositorySetup = (IHttpCrawledRepositorySetup) httpCrawledRepositorySetup.getImmutable();
			}
		}
		urlLastLength = urlDialogField.getText().length();
		return status;
	}

	/**
	 * Test url connection.
	 * 
	 * @param testURL
	 *            the test url
	 * 
	 * @return the int
	 */
	private int testURLConnection(String testURL) {
		int result = -1;
		try {
			URL u = new URL(testURL);
			HttpURLConnection uc = (HttpURLConnection) u.openConnection(IOToolBox.determineProxy(u));
			if (uc.getResponseCode() == HttpURLConnection.HTTP_OK) {
				result = CONNECTION_OK;
			} else {
				result = RESPONSE_STATE_NOT_200;
			}
		} catch (IllegalArgumentException iae) {
			logger.debug(iae);
			result = URL_NOT_CORRECT;
		} catch (MalformedURLException e) {
			logger.debug(e);
			result = URL_NOT_CORRECT;
		} catch (IOException e) {
			logger.debug(e);
			result = CANNOT_REACH_URL;
		}

		return result;
	}

	/**
	 * Test url connection.
	 * 
	 * @param testURL
	 *            the test url
	 * @param proxy
	 *            the proxy
	 * 
	 * @return the int
	 * @deprecated no more proxy configuration.
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private int testURLConnection(String testURL, Proxy proxy) {
		int result = -1;
		try {
			URL u = new URL(testURL);
			HttpURLConnection uc = (HttpURLConnection) u.openConnection(proxy);
			if (uc.getResponseCode() == HttpURLConnection.HTTP_OK) {
				result = CONNECTION_OK;
			} else {
				result = RESPONSE_STATE_NOT_200;
			}
		} catch (IllegalArgumentException iae) {
			logger.debug(iae);
			result = URL_NOT_CORRECT;
		} catch (MalformedURLException e) {
			logger.debug(e);
			result = URL_NOT_CORRECT;
		} catch (IOException e) {
			logger.debug(e);
			result = CANNOT_REACH_URL;
		}
		return result;
	}

	/**
	 * Adds the group filters.
	 * 
	 * @param repositorySetup
	 *            the repository setup
	 * @param filters
	 *            the filters
	 * 
	 * @return the i http repository setup
	 * 
	 * @see org.org.eclipse.dws.ui.internal.wizards.pages.AbstractRepositoryInfosWizardPage#addGroupFilters(org.org.repository.crawler.items.ICrawledRepositorySetup, java.util.Set)
	 */
	@Override
	protected IHttpCrawledRepositorySetup addGroupFilters(IHttpCrawledRepositorySetup repositorySetup, Set<String> filters) {
		HttpCrawledRepositorySetup httpCrawledRepositorySetup = new HttpCrawledRepositorySetup(repositorySetup);
		httpCrawledRepositorySetup.setGroupFilters(filters);
		return (IHttpCrawledRepositorySetup) httpCrawledRepositorySetup.getImmutable();
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
	 * Handle dialog field changed.
	 * 
	 * @param field
	 *            the field
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#handleDialogFieldChanged(org.org.eclipse.core.utils.platform.fields.IDialogField)
	 */
	@Override
	protected void handleDialogFieldChanged(IDialogField field) {
		if (field.equals(patternSetField)) {
			ComboDialogField comboDialogField = (ComboDialogField) field;
			if (comboDialogField.getSelectionIndex() >= 0) {
				String selectedItemLabel = comboDialogField.getItems()[comboDialogField.getSelectionIndex()];
				mapPatternSet(DWSCorePlugin.getDefault().getPatternSetWithLabel(selectedItemLabel));
			}
		}
		touch();
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
	 * Prepare test plugin.
	 * 
	 * @return the http repository browser plugin
	 */
	private HttpRepositoryBrowserPlugin prepareTestPlugin() {
		HttpRepositoryBrowserPlugin httpRepositoryBrowserPlugin = new HttpRepositoryBrowserPlugin();
		String url = urlDialogField.getText();
		HttpCrawledRepositorySetup httpCrawledRepositorySetup = new HttpCrawledRepositorySetup(url);
		try {
			Proxy proxy = IOToolBox.determineProxy(new URL(url));
			if (proxy != null && proxy.type() == Proxy.Type.HTTP && proxy.address() instanceof InetSocketAddress) {
				InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
				httpCrawledRepositorySetup.setProxyHost(inetSocketAddress.getHostName());
				httpCrawledRepositorySetup.setProxyPort(inetSocketAddress.getPort());
			}
		} catch (MalformedURLException e) {
			// do something deeply meaningful here
		}
		httpCrawledRepositorySetup.setPatternSet(new ImmutablePatternSet(patternSetField.getText(), entryPatternDialogField.getText(), fileEntryPatternDialogField.getText(), directoryEntryPatternDialogField.getText(), parentPatternDialogField.getText()));
		httpRepositoryBrowserPlugin.init(httpCrawledRepositorySetup);
		return httpRepositoryBrowserPlugin;
	}

}