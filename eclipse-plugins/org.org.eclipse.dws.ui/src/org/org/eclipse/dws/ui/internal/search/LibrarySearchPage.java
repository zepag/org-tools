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
package org.org.eclipse.dws.ui.internal.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.org.eclipse.dws.ui.DWSUIPlugin;

/**
 * The Class LibrarySearchPage.
 */
public class LibrarySearchPage extends DialogPage implements ISearchPage {

	/** The Constant EXTENSION_POINT_ID. */
	public static final String EXTENSION_POINT_ID = LibrarySearchPage.class.getName();

	/** The Constant SEARCH_FOR_LIBRARY. */
	public static final int SEARCH_FOR_LIBRARY = 0;

	/** The Constant HISTORY_SIZE. */
	private static final int HISTORY_SIZE = 12;

	// Dialog store id constants
	/** The Constant PAGE_NAME. */
	private final static String PAGE_NAME = "Maven2LibsSearchPage";

	/** The Constant STORE_HISTORY. */
	private final static String STORE_HISTORY = "HISTORY";

	/** The Constant STORE_HISTORY_SIZE. */
	private final static String STORE_HISTORY_SIZE = "HISTORY_SIZE";

	/** The previous search patterns. */
	private final List<SearchData> previousSearchPatterns = new ArrayList<SearchData>();

	/** The first time. */
	private boolean firstTime = true;

	/** The dialog settings. */
	private IDialogSettings dialogSettings;

	/** The expression combo. */
	private Combo expressionCombo;

	/** The search container. */
	private ISearchPageContainer searchContainer;

	/** The status label. */
	private CLabel statusLabel;

	/**
	 * Instantiates a new library search page.
	 */
	public LibrarySearchPage() {
		// required
	}

	/**
	 * Instantiates a new library search page.
	 * 
	 * @param title
	 *            the title
	 */
	public LibrarySearchPage(String title) {
		super(title);
	}

	/**
	 * Instantiates a new library search page.
	 * 
	 * @param title
	 *            the title
	 * @param image
	 *            the image
	 */
	public LibrarySearchPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Gets the previous search patterns.
	 * 
	 * @return the previous search patterns
	 */
	private String[] getPreviousSearchPatterns() {

		// Search results are not persistent
		int patternCount = previousSearchPatterns.size();
		String[] patterns = new String[patternCount];
		for (int i = 0; i < patternCount; i++) {
			patterns[i] = (previousSearchPatterns.get(i)).getPattern();
		}
		return patterns;
	}

	/**
	 * Gets the pattern.
	 * 
	 * @return the pattern
	 */
	private String getPattern() {
		return expressionCombo.getText();
	}

	/**
	 * Find in previous.
	 * 
	 * @param pattern
	 *            the pattern
	 * 
	 * @return the search data
	 */
	private SearchData findInPrevious(String pattern) {
		for (SearchData element : previousSearchPatterns) {
			if (pattern.equals(element.getPattern())) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Returns search pattern data and update previous searches. An existing entry will be updated.
	 * 
	 * @return the pattern data
	 */
	private SearchData getPatternData() {
		String pattern = getPattern();
		SearchData match = findInPrevious(pattern);
		if (match != null) {
			previousSearchPatterns.remove(match);
		}
		match = new SearchData(pattern);

		previousSearchPatterns.add(0, match); // insert on top
		return match;
	}

	/**
	 * @see org.eclipse.search.ui.ISearchPage#setContainer(org.eclipse.search.ui.ISearchPageContainer)
	 */
	public void setContainer(ISearchPageContainer container) {
		this.searchContainer = container;
	}

	/**
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible && expressionCombo != null) {
			if (firstTime) {
				firstTime = false;
				// Set item and text here to prevent page from resizing
				expressionCombo.setItems(getPreviousSearchPatterns());

			}
			expressionCombo.setFocus();
		}
		updateOKStatus();
		super.setVisible(visible);
	}

	/**
	 * @see org.eclipse.search.ui.ISearchPage#performAction()
	 */
	public boolean performAction() {
		SearchData data = getPatternData();

		// Setup search scope
		LibrarySearchScope scope = LibrarySearchScope.newSearchScope();

		ISearchQuery query = null;
		query = new LibraryNameQuery(scope, data.getPattern());

		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(query);
		return true;
	}

	/**
	 * Returns the page settings for this Java search page.
	 * 
	 * @return the page settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = DWSUIPlugin.getDefault().getDialogSettings();
		dialogSettings = settings.getSection(PAGE_NAME);
		if (dialogSettings == null) {
			dialogSettings = settings.addNewSection(PAGE_NAME);
		}
		return dialogSettings;
	}

	/**
	 * Initializes itself from the stored page settings.
	 */
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();
		try {
			int historySize = s.getInt(STORE_HISTORY_SIZE);
			for (int i = 0; i < historySize; i++) {
				IDialogSettings histSettings = s.getSection(STORE_HISTORY + i);
				if (histSettings != null) {
					SearchData data = SearchData.create(histSettings);
					if (data != null) {
						previousSearchPatterns.add(data);
					}
				}
			}
		} catch (NumberFormatException e) {
			// ignore
		}
	}

	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s = getDialogSettings();
		int historySize = Math.min(previousSearchPatterns.size(), HISTORY_SIZE);
		s.put(STORE_HISTORY_SIZE, historySize);
		for (int i = 0; i < historySize; i++) {
			IDialogSettings histSettings = s.addNewSection(STORE_HISTORY + i);
			SearchData data = (previousSearchPatterns.get(i));
			data.store(histSettings);
		}
	}

	/**
	 * Creates the page's content.
	 * 
	 * @param parent
	 *            the parent
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		readConfiguration();

		Composite result = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		result.setLayout(layout);

		Control expressionComposite = createExpression(result);
		expressionComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		Label separator = new Label(result, SWT.NONE);
		separator.setVisible(false);
		GridData data = new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1);
		data.heightHint = convertHeightInCharsToPixels(1) / 3;
		separator.setLayoutData(data);

		setControl(result);

		Dialog.applyDialogFont(result);
	}

	/**
	 * Creates the expression.
	 * 
	 * @param parent
	 *            the parent
	 * 
	 * @return the control
	 */
	private Control createExpression(Composite parent) {
		// Group with grid layout with 2 columns
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		group.setLayout(layout);

		// Expression text + info
		Label label = new Label(group, SWT.LEFT);
		label.setText(LibrarySearchMessages.SearchPage_expression);
		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1));
		// Expression combo
		expressionCombo = new Combo(group, SWT.SINGLE | SWT.BORDER);
		expressionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handlePatternSelected();
				updateOKStatus();
			}
		});
		expressionCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateOKStatus();
			}
		});
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1);
		data.widthHint = convertWidthInCharsToPixels(50);
		expressionCombo.setLayoutData(data);
		// Text line which explains the special characters
		statusLabel = new CLabel(group, SWT.LEAD);
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		statusLabel.setFont(group.getFont());
		statusLabel.setAlignment(SWT.LEFT);
		statusLabel.setText(LibrarySearchMessages.SearchPage_expressionHint);
		return group;
	}

	/**
	 * Update ok status.
	 */
	final void updateOKStatus() {
		boolean isValid = isValidSearchPattern();
		searchContainer.setPerformActionEnabled(isValid);
	}

	/**
	 * Checks if is valid search pattern.
	 * 
	 * @return true, if is valid search pattern
	 */
	private boolean isValidSearchPattern() {
		if (getPattern().length() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		writeConfiguration();
		super.dispose();
	}

	/**
	 * Handle pattern selected.
	 */
	private void handlePatternSelected() {
		int selectionIndex = expressionCombo.getSelectionIndex();
		if (selectionIndex < 0 || selectionIndex >= previousSearchPatterns.size()) {
			return;
		}
		SearchData data = previousSearchPatterns.get(selectionIndex);
		expressionCombo.setText(data.getPattern());
		searchContainer.setSelectedScope(ISearchPageContainer.WORKSPACE_SCOPE);
	}

	/**
	 * The Class SearchData.
	 */
	private static class SearchData {

		/** The pattern. */
		private String pattern;

		/**
		 * Instantiates a new search data.
		 * 
		 * @param pattern
		 *            the pattern
		 */
		public SearchData(String pattern) {
			this.pattern = pattern;
		}

		/**
		 * Gets the pattern.
		 * 
		 * @return the pattern
		 */
		public String getPattern() {
			return pattern;
		}

		/**
		 * Store.
		 * 
		 * @param settings
		 *            the settings
		 */
		public void store(IDialogSettings settings) {
			settings.put("pattern", pattern);
		}

		/**
		 * Creates the.
		 * 
		 * @param settings
		 *            the settings
		 * 
		 * @return the search data
		 */
		public static SearchData create(IDialogSettings settings) {
			String pattern = settings.get("pattern");
			if (pattern.length() == 0) {
				return null;
			}

			try {
				return new SearchData(pattern);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}

}
