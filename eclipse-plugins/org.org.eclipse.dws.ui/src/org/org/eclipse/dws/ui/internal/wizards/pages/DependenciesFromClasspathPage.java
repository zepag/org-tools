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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.dialogs.selection.IListDialogField;
import org.org.eclipse.core.utils.platform.fields.IDialogField;
import org.org.eclipse.core.utils.platform.wizards.StatusInfo;
import org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage;
import org.org.eclipse.core.utils.platform.wizards.page.WizardContentsHelper;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationConstants;
import org.org.eclipse.dws.core.internal.versioning.ArtifactVersionComparator;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;
import org.org.repository.crawler.maven2.model.GroupsHolder;

/**
 * This wizard allows to deduce dependencies from project's classpath.
 */
public class DependenciesFromClasspathPage extends AbstractWizardCustomPage {

	/**
	 * The Class ArtifactVersionWrappersComparator.
	 */
	public static final class ArtifactVersionWrappersComparator implements Comparator<ArtifactVersionWrapper> {

		/** The library id. */
		public final String libraryId;

		/**
		 * Instantiates a new artifact version wrappers comparator.
		 * 
		 * @param libraryId
		 *            the library id
		 */
		public ArtifactVersionWrappersComparator(String libraryId) {
			this.libraryId = libraryId;
		}

		/**
		 * Compare.
		 * 
		 * @param o1
		 *            the o1
		 * @param o2
		 *            the o2
		 * 
		 * @return the int
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(ArtifactVersionWrapper o1, ArtifactVersionWrapper o2) {
			Integer resemblanceFactor1 = getResemblanceFactor(libraryId, o1.getArtifactVersion().getId());
			Integer resemblanceFactor2 = getResemblanceFactor(libraryId, o2.getArtifactVersion().getId());
			int result = -resemblanceFactor1.compareTo(resemblanceFactor2);
			if (result == 0) {
				result = o1.getArtifactVersion().getId().compareTo(o2.getArtifactVersion().getId());
				if (result == 0) {
					result = -(o1.getExactMatch().compareTo(o2.getExactMatch()));
				}
			}
			return result;
		}

		/**
		 * Gets the resemblance factor.
		 * 
		 * @param libraryId
		 *            the library id
		 * @param artifactVersionId
		 *            the artifact version id
		 * 
		 * @return the resemblance factor
		 */
		private static int getResemblanceFactor(String libraryId, String artifactVersionId) {
			int result = 0;
			for (int count = 0; (count < libraryId.length() && count < artifactVersionId.length()); count++) {
				if (libraryId.charAt(count) == artifactVersionId.charAt(count)) {
					result++;
				} else {
					break;
				}
			}
			return result;
		}
	}

	/**
	 * The Class PossibleDependencyWrapper is a wrapper for a possible dependency.<br>
	 * This is a holder for all the different possible matches of a classpath library.
	 */
	public static class PossibleDependencyWrapper {

		/** The library path. */
		private final String libraryPath;

		/** The library id. */
		private final String libraryId;

		/** The artifact version wrappers. */
		private final Set<ArtifactVersionWrapper> artifactVersionWrappers;

		/** The exact match. */
		private final Boolean exactMatch;

		/**
		 * Instantiates a new possible dependency wrapper.
		 * 
		 * @param libraryPath
		 *            the library path
		 * @param libraryId
		 *            the library id
		 * @param artifactVersionWrappers
		 *            the artifact version wrappers
		 * @param exactMatch
		 *            the exact match
		 */
		public PossibleDependencyWrapper(String libraryPath, String libraryId, Set<ArtifactVersionWrapper> artifactVersionWrappers, Boolean exactMatch) {
			this.libraryPath = libraryPath;
			this.libraryId = libraryId;
			this.artifactVersionWrappers = artifactVersionWrappers;
			this.exactMatch = exactMatch;
		}

		/**
		 * Gets the artifact version wrappers.
		 * 
		 * @return the artifact version wrappers
		 */
		public Set<ArtifactVersionWrapper> getArtifactVersionWrappers() {
			return artifactVersionWrappers;
		}

		/**
		 * Gets the exact match.
		 * 
		 * @return the exact match
		 */
		public Boolean getExactMatch() {
			return exactMatch;
		}

		/**
		 * Gets the library id.
		 * 
		 * @return the library id
		 */
		public String getLibraryId() {
			return libraryId;
		}

		/**
		 * Gets the library path.
		 * 
		 * @return the library path
		 */
		public String getLibraryPath() {
			return libraryPath;
		}

	}

	/**
	 * The Class ArtifactVersionWrapper is a wrapper for a matching Artifact Version.
	 */
	public static class ArtifactVersionWrapper {

		/** The library id. */
		private final String libraryId;

		/** The artifact version. */
		private final ArtifactVersion artifactVersion;

		/** The exact match. */
		private final Boolean exactMatch;

		/** The selected. */
		private AtomicBoolean selected;

		/**
		 * Instantiates a new artifact version wrapper.
		 * 
		 * @param artifactVersion
		 *            the artifact version
		 * @param exactMatch
		 *            the exact match
		 * @param libraryId
		 *            the library id
		 */
		public ArtifactVersionWrapper(ArtifactVersion artifactVersion, Boolean exactMatch, String libraryId) {
			this.artifactVersion = artifactVersion;
			this.exactMatch = exactMatch;
			this.libraryId = libraryId;
			if (exactMatch) {
				selected = new AtomicBoolean(true);
			} else {
				selected = new AtomicBoolean(false);
			}
		}

		/**
		 * Gets the artifact version.
		 * 
		 * @return the artifact version
		 */
		public ArtifactVersion getArtifactVersion() {
			return artifactVersion;
		}

		/**
		 * Gets the exact match.
		 * 
		 * @return the exact match
		 */
		public Boolean getExactMatch() {
			return exactMatch;
		}

		/**
		 * Checks if is selected.
		 * 
		 * @return the boolean
		 */
		public Boolean isSelected() {
			return selected.get();
		}

		/**
		 * Invert selection.
		 */
		public void invertSelection() {
			selected.getAndSet(!selected.get());
		}

		/**
		 * Gets the uID.
		 * 
		 * @return the uID
		 */
		public String getUID() {
			return libraryId + artifactVersion.getUID();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append((exactMatch ? "[EXACT_MATCH]" : "") + (selected.get() ? "[SELECTED]" : "") + artifactVersion.getId());
			return stringBuilder.toString();
		}
	}

	/**
	 * The listener interface for receiving artifactVersionMouse events. The class that is interested in processing a artifactVersionMouse event implements this interface, and the object created with that class is registered with a component using the component's <code>addArtifactVersionMouseListener<code> method. When
	 * the artifactVersionMouse event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see ArtifactVersionMouseEvent
	 */
	public static class ArtifactVersionMouseListener implements MouseListener {

		/** The artifact version wrapper. */
		private final ArtifactVersionWrapper artifactVersionWrapper;

		/**
		 * Instantiates a new artifact version mouse listener.
		 * 
		 * @param artifactVersionWrapper
		 *            the artifact version wrapper
		 */
		public ArtifactVersionMouseListener(ArtifactVersionWrapper artifactVersionWrapper) {
			this.artifactVersionWrapper = artifactVersionWrapper;
		}

		/**
		 * Mouse up.
		 * 
		 * @param e
		 *            the e
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {
			artifactVersionWrapper.invertSelection();
		}

		/**
		 * Mouse down.
		 * 
		 * @param e
		 *            the e
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent e) {
		}

		/**
		 * Mouse double click.
		 * 
		 * @param e
		 *            the e
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
		}

	}

	/** The Constant WIZARD_PAGE_ID. */
	public static final String WIZARD_PAGE_ID = DependenciesFromClasspathPage.class.getName();

	/** The chosen libraries. */
	private final Set<ArtifactVersionWrapper> chosenLibraries;

	/** The PROJECT. */
	private final IProject PROJECT;

	/** The dependencies expand item. */
	private ExpandItem dependenciesExpandItem;

	/** The filtered libraries. */
	private final Set<String> filteredLibraries = new LinkedHashSet<String>();

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param project
	 *            the project
	 */
	public DependenciesFromClasspathPage(IProject project) {
		super(WIZARD_PAGE_ID, WizardsMessages.DependenciesFromClasspathPage_title, WizardsMessages.DependenciesFromClasspathPage_description);
		setColumnsNumber(1);
		this.PROJECT = project;
		chosenLibraries = new TreeSet<ArtifactVersionWrapper>(new Comparator<ArtifactVersionWrapper>() {

			public int compare(ArtifactVersionWrapper o1, ArtifactVersionWrapper o2) {
				int result = o1.getArtifactVersion().getId().compareTo(o2.getArtifactVersion().getId());
				if (o1.getExactMatch() != o2.getExactMatch()) {
					result = -(o1.getExactMatch().compareTo(o2.getExactMatch()));
				}
				return result;
			}

		});
		StringTokenizer stringTokenizer = new StringTokenizer(AggregatedProperties.getWizardFilteredJars(), ConfigurationConstants.PIPE_SEPARATOR, false);
		while (stringTokenizer.hasMoreTokens()) {
			filteredLibraries.add(stringTokenizer.nextToken());
		}
	}

	/**
	 * Describe.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#describe()
	 */
	@Override
	protected void describe() {
		ExpandBar expandBar = WizardContentsHelper.createExpandBar(getWizardContainer());
		expandBar.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite librariesComposite = describeLibrariesSection(expandBar);
		librariesComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		dependenciesExpandItem = WizardContentsHelper.createExpandItem(expandBar, librariesComposite, WizardsMessages.DependenciesFromClasspathPage_dependencies);
		dependenciesExpandItem.setExpanded(false);
	}

	/**
	 * Describe libraries section.
	 * 
	 * @param expandBar
	 *            the expand bar
	 * 
	 * @return the composite
	 */
	private Composite describeLibrariesSection(ExpandBar expandBar) {
		final Composite composite = WizardContentsHelper.createClientComposite(expandBar);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);

		IJavaProject javaProject = JavaCore.create(PROJECT);
		try {
			Set<PossibleDependencyWrapper> possibleDependencyWrappers = getPossibleDependencyWrappers(javaProject);
			for (final PossibleDependencyWrapper possibleDependencyWrapper : possibleDependencyWrappers) {
				// HIDE APPROXIMATIVE MATCHES IF SPECIFIED IN PREFERENCES, AND PRINT A MESSAGE INSTEAD
				if (AggregatedProperties.getHideApproximativeMatch() && !possibleDependencyWrapper.getExactMatch()) {
					createNoExactMatchLabel(composite, possibleDependencyWrapper);
				} else {
					createClasspathEntryLabel(composite, possibleDependencyWrapper);
					final Composite toBeScrolledcomposite = createPossibleMatchesComposite(composite);
					int count = 0;
					for (final ArtifactVersionWrapper artifactVersionWrapper : possibleDependencyWrapper.getArtifactVersionWrappers()) {
						count++;
						if (count <= AggregatedProperties.getNumberOfKeptMatches()) {
							chosenLibraries.add(artifactVersionWrapper);
							final Button button = createPossibleMatchCheckbox(toBeScrolledcomposite, artifactVersionWrapper);
							createPossibleMatchLabel(toBeScrolledcomposite, artifactVersionWrapper);
							GridData layoutData3 = new GridData(SWT.FILL, SWT.NONE, true, false);
							button.setLayoutData(layoutData3);
						} else {
							createNMoreEntriesLabel(toBeScrolledcomposite, possibleDependencyWrapper, count);
							break;
						}
					}
					toBeScrolledcomposite.setSize(toBeScrolledcomposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}
		} catch (JavaModelException e) {
			ErrorDialog errorDialog = new ErrorDialog(WizardsMessages.DependenciesFromClasspathPage_error_parsing_title, WizardsMessages.DependenciesFromClasspathPage_unexpected_error, e);
			errorDialog.open();
		} catch (Exception e) {
			ErrorDialog errorDialog = new ErrorDialog(WizardsMessages.DependenciesFromClasspathPage_error_parsing_title, WizardsMessages.DependenciesFromClasspathPage_unexpected_error, e);
			errorDialog.open();
		}
		if (chosenLibraries.size() == 0) {
			WizardContentsHelper.createDescriptionLabel(composite, WizardsMessages.DependenciesFromClasspathPage_noAvailableLibrary);
		}
		return composite;
	}

	/**
	 * Creates the possible match label.
	 * 
	 * @param toBeScrolledcomposite
	 *            the to be scrolledcomposite
	 * @param artifactVersionWrapper
	 *            the artifact version wrapper
	 */
	private void createPossibleMatchLabel(final Composite toBeScrolledcomposite, final ArtifactVersionWrapper artifactVersionWrapper) {
		final Label buttonLabel = new Label(toBeScrolledcomposite, SWT.NONE);
		buttonLabel.setBackground(getShell().getDisplay().getSystemColor(artifactVersionWrapper.getExactMatch() ? SWT.COLOR_GREEN : SWT.COLOR_WHITE));
		Artifact artifact = artifactVersionWrapper.getArtifactVersion().getParent();
		Group group = artifact.getParent();
		GroupsHolder groupsHolder = (GroupsHolder) group.getParent();
		buttonLabel.setText((artifactVersionWrapper.getExactMatch() ? WizardsMessages.DependenciesFromClasspathPage_exactMatch : "") + MessageFormat.format(WizardsMessages.DependenciesFromClasspathPage_artifactDescription, new Object[] { group.getName(), artifactVersionWrapper.getArtifactVersion().getId(), groupsHolder.getUID() }));
	}

	/**
	 * Creates the possible match checkbox.
	 * 
	 * @param toBeScrolledcomposite
	 *            the to be scrolledcomposite
	 * @param artifactVersionWrapper
	 *            the artifact version wrapper
	 * 
	 * @return the button
	 */
	private Button createPossibleMatchCheckbox(final Composite toBeScrolledcomposite, final ArtifactVersionWrapper artifactVersionWrapper) {
		final Button button = new Button(toBeScrolledcomposite, SWT.CHECK);
		GridData layoutData2 = new GridData(SWT.FILL, SWT.NONE, true, false);
		button.setLayoutData(layoutData2);
		button.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		if (artifactVersionWrapper.getExactMatch()) {
			button.setSelection(true);
		}
		button.addMouseListener(new ArtifactVersionMouseListener(artifactVersionWrapper));
		return button;
	}

	/**
	 * Creates the possible matches composite.
	 * 
	 * @param composite
	 *            the composite
	 * 
	 * @return the composite
	 */
	private Composite createPossibleMatchesComposite(final Composite composite) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL);
		scrolledComposite.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		final Composite toBeScrolledcomposite = new Composite(scrolledComposite, SWT.NONE);
		toBeScrolledcomposite.setLayout(new GridLayout(2, false));
		toBeScrolledcomposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrolledComposite.setContent(toBeScrolledcomposite);
		toBeScrolledcomposite.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		return toBeScrolledcomposite;
	}

	/**
	 * Creates the classpath entry label.
	 * 
	 * @param composite
	 *            the composite
	 * @param possibleDependencyWrapper
	 *            the possible dependency wrapper
	 */
	private void createClasspathEntryLabel(final Composite composite, final PossibleDependencyWrapper possibleDependencyWrapper) {
		final Label label = new Label(composite, SWT.FLAT | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		layoutData.horizontalSpan = 1;
		label.setLayoutData(layoutData);
		label.setBackground(getShell().getDisplay().getSystemColor(possibleDependencyWrapper.getExactMatch() ? SWT.COLOR_DARK_GRAY : SWT.COLOR_DARK_RED));
		label.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		label.setText(possibleDependencyWrapper.getLibraryId() + " " + (possibleDependencyWrapper.getLibraryPath())); //$NON-NLS-1$
	}

	/**
	 * Creates the n more entries label.
	 * 
	 * @param composite
	 *            the composite
	 * @param possibleDependencyWrapper
	 *            the possible dependency wrapper
	 * @param count
	 *            the count
	 */
	private void createNMoreEntriesLabel(final Composite composite, final PossibleDependencyWrapper possibleDependencyWrapper, Integer count) {
		final Label label = new Label(composite, SWT.FLAT | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		layoutData.horizontalSpan = 2;
		label.setLayoutData(layoutData);
		label.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		label.setText(possibleDependencyWrapper.getArtifactVersionWrappers().size() - count + " more possible dependencies found..."); //$NON-NLS-1$
	}

	/**
	 * Creates the no exact match label.
	 * 
	 * @param composite
	 *            the composite
	 * @param possibleDependencyWrapper
	 *            the possible dependency wrapper
	 */
	private void createNoExactMatchLabel(final Composite composite, final PossibleDependencyWrapper possibleDependencyWrapper) {
		Label label = new Label(composite, SWT.FLAT | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
		layoutData.horizontalSpan = 1;
		label.setLayoutData(layoutData);
		label.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		label.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		label.setText(MessageFormat.format(WizardsMessages.DependenciesFromClasspathPage_noExactMatch, new Object[] { possibleDependencyWrapper.getLibraryId() }));
	}

	/**
	 * Gets the possible dependency wrappers.
	 * 
	 * @param javaProject
	 *            the java project
	 * 
	 * @return the possible dependency wrappers
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	private Set<PossibleDependencyWrapper> getPossibleDependencyWrappers(IJavaProject javaProject) throws JavaModelException {
		Set<IPackageFragmentRoot> packageFragmentRoots = new TreeSet<IPackageFragmentRoot>(new Comparator<IPackageFragmentRoot>() {
			public int compare(IPackageFragmentRoot o1, IPackageFragmentRoot o2) {
				return o1.getElementName().compareTo(o2.getElementName());
			}

		});
		for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
			if (isPossibleDependency(packageFragmentRoot)) {
				packageFragmentRoots.add(packageFragmentRoot);
			}
		}
		Set<PossibleDependencyWrapper> possibleDependencyWrappers = new TreeSet<PossibleDependencyWrapper>(new Comparator<PossibleDependencyWrapper>() {

			public int compare(PossibleDependencyWrapper o1, PossibleDependencyWrapper o2) {
				int result = o1.getLibraryId().compareTo(o2.getLibraryId());
				if (o1.getExactMatch() != o2.getExactMatch()) {
					result = -(o1.getExactMatch().compareTo(o2.getExactMatch()));
				}
				return result;
			}

		});
		for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
			String libraryPath = packageFragmentRoot.getPath().toOSString();
			final String libraryId = packageFragmentRoot.getElementName();
			Set<ArtifactVersion> possibleDependencies = findPossibleDependencies(libraryId);
			if (possibleDependencies.size() > 0) {
				Set<ArtifactVersionWrapper> artifactVersionWrappers = new TreeSet<ArtifactVersionWrapper>(new ArtifactVersionWrappersComparator(libraryId));
				wrapArtifactVersions(possibleDependencies, artifactVersionWrappers, libraryId);
				boolean hasExactMatch = lookForExactMatch(artifactVersionWrappers);
				possibleDependencyWrappers.add(new PossibleDependencyWrapper(libraryPath, libraryId, Collections.unmodifiableSet(artifactVersionWrappers), hasExactMatch));
			}
		}
		possibleDependencyWrappers = Collections.unmodifiableSet(possibleDependencyWrappers);
		return possibleDependencyWrappers;
	}

	/**
	 * Look for exact match.
	 * 
	 * @param artifactVersionWrappers
	 *            the artifact version wrappers
	 * 
	 * @return the boolean
	 */
	private Boolean lookForExactMatch(Set<ArtifactVersionWrapper> artifactVersionWrappers) {
		Boolean result = false;
		for (ArtifactVersionWrapper artifactVersionWrapper : artifactVersionWrappers) {
			if (artifactVersionWrapper.getExactMatch()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Wrap artifact versions.
	 * 
	 * @param possibleDependencies
	 *            the possible dependencies
	 * @param artifactVersionWrappers
	 *            the artifact version wrappers
	 * @param libraryId
	 *            the library id
	 */
	private void wrapArtifactVersions(Set<ArtifactVersion> possibleDependencies, Set<ArtifactVersionWrapper> artifactVersionWrappers, String libraryId) {
		for (ArtifactVersion artifactVersion : possibleDependencies) {
			artifactVersionWrappers.add(new ArtifactVersionWrapper(artifactVersion, artifactVersion.getId().startsWith(libraryId), libraryId));
		}
	}

	/**
	 * Checks if is possible dependency.
	 * 
	 * @param packageFragmentRoot
	 *            the package fragment root
	 * 
	 * @return the boolean
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	private Boolean isPossibleDependency(IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {
		boolean isArchive = packageFragmentRoot.isArchive();
		int entryKind = packageFragmentRoot.getRawClasspathEntry().getEntryKind();
		boolean isFiltered = isFiltered(packageFragmentRoot);
		return isArchive && (entryKind != IClasspathEntry.CPE_PROJECT) && entryKind != IClasspathEntry.CPE_SOURCE && !isFiltered;
	}

	/**
	 * Checks if is filtered.
	 * 
	 * @param packageFragmentRoot
	 *            the package fragment root
	 * 
	 * @return true, if is filtered
	 */
	private boolean isFiltered(IPackageFragmentRoot packageFragmentRoot) {
		Boolean result = false;
		for (String filteredEntry : filteredLibraries) {
			if (packageFragmentRoot.getElementName().endsWith(filteredEntry)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Find possible dependencies.
	 * 
	 * @param libraryId
	 *            the library id
	 * 
	 * @return the set< artifact version>
	 */
	private Set<ArtifactVersion> findPossibleDependencies(String libraryId) {
		Set<ArtifactVersion> resultList = new TreeSet<ArtifactVersion>(new ArtifactVersionComparator());
		libraryId = removeExtension(libraryId);
		Set<String> searchCriteria = extractSearchCriteria(libraryId);
		for (String searchCriterion : searchCriteria) {
			resultList.addAll(RepositoryModelUtils.lookForArtifactVersionInAllRepositories(searchCriterion));
		}
		return resultList;
	}

	/**
	 * Removes the extension.
	 * 
	 * @param libraryId
	 *            the library id
	 * 
	 * @return the string
	 */
	private String removeExtension(String libraryId) {
		return libraryId.substring(0, libraryId.lastIndexOf('.'));
	}

	/**
	 * Extract search criteria.
	 * 
	 * @param libraryId
	 *            the library id
	 * 
	 * @return the set< string>
	 */
	private Set<String> extractSearchCriteria(String libraryId) {
		Set<String> searchCriteria = new LinkedHashSet<String>();
		StringTokenizer tkz = new StringTokenizer(libraryId, "-", false); //$NON-NLS-1$
		String builtCriterion = tkz.nextToken();
		searchCriteria.add(builtCriterion);
		while (tkz.hasMoreTokens()) {
			String nextToken = tkz.nextToken();
			builtCriterion = builtCriterion + "-" + nextToken; //$NON-NLS-1$
			searchCriteria.add(builtCriterion);
		}
		return searchCriteria;
	}

	/**
	 * Initialize.
	 * 
	 * @see org.org.eclipse.core.utils.platform.wizards.page.AbstractWizardCustomPage#initialize()
	 */
	@Override
	protected void initialize() {
	}

	/**
	 * Sets the visible.
	 * 
	 * @param visible
	 *            the visible
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		dependenciesExpandItem.setExpanded(visible);
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
		IStatus status = null;
		status = new StatusInfo(IStatus.OK, WizardsMessages.DependenciesFromClasspathPage_chooseComputedLibrary);
		status = validateFieldsFormat(status);
		return status;
	}

	/**
	 * Validate fields format.
	 * 
	 * @param status
	 *            the status
	 * 
	 * @return the i status
	 */
	private IStatus validateFieldsFormat(IStatus status) {
		IStatus result = status;
		return result;
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
		touch();
	}

	/**
	 * Gets the chosen dependencies.
	 * 
	 * @return the chosen dependencies
	 */
	public Set<ArtifactVersion> getChosenDependencies() {
		Set<ArtifactVersion> result = new LinkedHashSet<ArtifactVersion>();
		for (ArtifactVersionWrapper chosenDependency : chosenLibraries) {
			if (chosenDependency.isSelected()) {
				result.add(chosenDependency.getArtifactVersion());
			}
		}
		return result;
	}

}