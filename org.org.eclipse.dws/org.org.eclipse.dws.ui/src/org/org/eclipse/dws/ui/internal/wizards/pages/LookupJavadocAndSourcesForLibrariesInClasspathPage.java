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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.org.eclipse.dws.core.internal.model.ArtifactVersionWrapper;
import org.org.eclipse.dws.core.internal.model.LibraryWithMissingJavadocOrSourcesWrapper;
import org.org.eclipse.dws.ui.internal.wizards.WizardsMessages;
import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;

/**
 * This wizard allows to lookup javadoc and sources of classpath libraries in the repositories.
 */
public class LookupJavadocAndSourcesForLibrariesInClasspathPage extends AbstractWizardCustomPage {

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
					result = 1;
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
	public static final String WIZARD_PAGE_ID = LookupJavadocAndSourcesForLibrariesInClasspathPage.class.getName();

	/** The chosen libraries. */
	private final Set<LibraryWithMissingJavadocOrSourcesWrapper> chosenLibraries;

	/** The PROJECT. */
	private final IJavaProject PROJECT;

	/** The javadoc expand item. */
	private ExpandItem gotJavadocExpandItem;
	/** The sources expand item. */
	private ExpandItem gotSourcesExpandItem;
	/** The none expand item. */
	private ExpandItem noneExpandItem;

	/** The filtered libraries. */
	private final Set<String> filteredLibraries = new LinkedHashSet<String>();

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param selectedProject
	 *            the selected project
	 */
	public LookupJavadocAndSourcesForLibrariesInClasspathPage(IJavaProject selectedProject) {
		super(WIZARD_PAGE_ID, WizardsMessages.DependenciesFromClasspathPage_title, WizardsMessages.DependenciesFromClasspathPage_description);
		setColumnsNumber(1);
		this.PROJECT = selectedProject;
		chosenLibraries = new TreeSet<LibraryWithMissingJavadocOrSourcesWrapper>(new Comparator<LibraryWithMissingJavadocOrSourcesWrapper>() {

			public int compare(LibraryWithMissingJavadocOrSourcesWrapper o1, LibraryWithMissingJavadocOrSourcesWrapper o2) {
				int result = o1.getLibraryPath().compareTo(o2.getLibraryPath());
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
		resolveLibraries();
		if (chosenLibraries.size() == 0) {
			WizardContentsHelper.createDescriptionLabel(getWizardContainer(), "No libraries");
		} else {
			final Composite composite = WizardContentsHelper.createClientComposite(getWizardContainer(), SWT.BORDER);
			GridLayout layout = new GridLayout(1, false);
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
			composite.setBackground(getWizardContainer().getDisplay().getSystemColor(SWT.COLOR_WHITE));
			List<LibraryWithMissingJavadocOrSourcesWrapper> javadocAvailableLibraries = resolveLibrariesWithJavadocAvailable();
			if (javadocAvailableLibraries.size() > 0) {
				ExpandBar expandBar = WizardContentsHelper.createExpandBar(composite);
				expandBar.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

				Composite gotJavadocComposite = describeGotJavadocSection(expandBar, javadocAvailableLibraries);
				gotJavadocComposite.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));

				gotJavadocExpandItem = WizardContentsHelper.createExpandItem(expandBar, gotJavadocComposite, "Got Javadoc!");
				gotJavadocExpandItem.setExpanded(true);
			}
			List<LibraryWithMissingJavadocOrSourcesWrapper> sourcesAvailableLibraries = resolveLibrariesWithSourcesAvailable();
			if (sourcesAvailableLibraries.size() > 0) {
				ExpandBar expandBar = WizardContentsHelper.createExpandBar(composite);
				expandBar.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

				Composite gotSourcesComposite = describeGotSourcesSection(expandBar, sourcesAvailableLibraries);
				gotSourcesComposite.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));

				gotSourcesExpandItem = WizardContentsHelper.createExpandItem(expandBar, gotSourcesComposite, "Got Sources!");
				gotSourcesExpandItem.setExpanded(true);
			}
			List<LibraryWithMissingJavadocOrSourcesWrapper> noneAvailableLibraries = resolveLibrariesWithNoneAvailable();
			if (noneAvailableLibraries.size() > 0) {
				ExpandBar expandBar = WizardContentsHelper.createExpandBar(composite);
				expandBar.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

				Composite gotSourcesComposite = describeNoneSection(expandBar, noneAvailableLibraries);
				gotSourcesComposite.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, true));

				noneExpandItem = WizardContentsHelper.createExpandItem(expandBar, gotSourcesComposite, "None found");
				noneExpandItem.setExpanded(false);
			}
		}
	}

	private List<LibraryWithMissingJavadocOrSourcesWrapper> resolveLibrariesWithNoneAvailable() {
		List<LibraryWithMissingJavadocOrSourcesWrapper> result = new ArrayList<LibraryWithMissingJavadocOrSourcesWrapper>();
		for (final LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper : chosenLibraries) {
			boolean gotJavadoc = possibleDependencyWrapper.getMissesJavadoc() && javadocInRepository(possibleDependencyWrapper);
			boolean gotSources = possibleDependencyWrapper.getMissesSources() && sourcesInRepository(possibleDependencyWrapper);
			if ((!gotJavadoc) && (!gotSources)) {
				result.add(possibleDependencyWrapper);
			}
		}
		return result;
	}

	private List<LibraryWithMissingJavadocOrSourcesWrapper> resolveLibrariesWithJavadocAvailable() {
		List<LibraryWithMissingJavadocOrSourcesWrapper> result = new ArrayList<LibraryWithMissingJavadocOrSourcesWrapper>();
		for (final LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper : chosenLibraries) {
			if (possibleDependencyWrapper.getMissesJavadoc() && javadocInRepository(possibleDependencyWrapper)) {
				result.add(possibleDependencyWrapper);
			}
		}
		return result;
	}

	private List<LibraryWithMissingJavadocOrSourcesWrapper> resolveLibrariesWithSourcesAvailable() {
		List<LibraryWithMissingJavadocOrSourcesWrapper> result = new ArrayList<LibraryWithMissingJavadocOrSourcesWrapper>();
		for (final LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper : chosenLibraries) {
			if (possibleDependencyWrapper.getMissesJavadoc() && sourcesInRepository(possibleDependencyWrapper)) {
				result.add(possibleDependencyWrapper);
			}
		}
		return result;
	}

	private Composite describeGotJavadocSection(ExpandBar expandBar, List<LibraryWithMissingJavadocOrSourcesWrapper> libraries) {
		final Composite composite = WizardContentsHelper.createClientComposite(expandBar);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		for (final LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper : libraries) {
			final Label label = new Label(composite, SWT.FLAT);
			GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
			layoutData.horizontalSpan = 1;
			label.setLayoutData(layoutData);
			label.setText(possibleDependencyWrapper.getLibraryId() + " " + (possibleDependencyWrapper.getLibraryPath()));
		}
		return composite;
	}

	private Composite describeGotSourcesSection(ExpandBar expandBar, List<LibraryWithMissingJavadocOrSourcesWrapper> libraries) {
		final Composite composite = WizardContentsHelper.createClientComposite(expandBar);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		for (final LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper : libraries) {
			final Label label = new Label(composite, SWT.FLAT);
			GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
			layoutData.horizontalSpan = 1;
			label.setLayoutData(layoutData);
			label.setText(possibleDependencyWrapper.getLibraryId() + " " + (possibleDependencyWrapper.getLibraryPath()));
		}
		return composite;
	}

	private Composite describeNoneSection(ExpandBar expandBar, List<LibraryWithMissingJavadocOrSourcesWrapper> libraries) {
		final Composite composite = WizardContentsHelper.createClientComposite(expandBar);
		GridLayout layout = new GridLayout(1, false);
		composite.setLayout(layout);
		for (final LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper : libraries) {
			final Label label = new Label(composite, SWT.FLAT);
			GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
			layoutData.horizontalSpan = 1;
			label.setLayoutData(layoutData);
			label.setText(possibleDependencyWrapper.getLibraryId() + " " + (possibleDependencyWrapper.getLibraryPath()));
		}
		return composite;
	}

	private boolean javadocInRepository(LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper) {
		boolean javadocAvailable = false;
		for (ArtifactVersionWrapper artifactVersionWrapper : possibleDependencyWrapper.getArtifactVersionWrappers()) {
			if (artifactVersionWrapper.getExactMatch() && artifactVersionWrapper.getArtifactVersion().getJavadocUrl() != null) {
				javadocAvailable = true;
				break;
			}
		}
		return javadocAvailable;
	}

	private boolean sourcesInRepository(LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper) {
		boolean sourcesAvailable = false;
		for (ArtifactVersionWrapper artifactVersionWrapper : possibleDependencyWrapper.getArtifactVersionWrappers()) {
			if (artifactVersionWrapper.getArtifactVersion().getSourcesUrl() != null && artifactVersionWrapper.getExactMatch()) {
				sourcesAvailable = true;
				break;
			}
		}
		return sourcesAvailable;
	}

	/**
	 * Describe libraries section.
	 * 
	 * @param expandBar
	 *            the expand bar
	 * 
	 * @return the composite
	 */
	private void resolveLibraries() {
		// final Composite composite = WizardContentsHelper.createClientComposite(expandBar);
		// GridLayout layout = new GridLayout(1, false);
		// composite.setLayout(layout);

		try {
			Set<LibraryWithMissingJavadocOrSourcesWrapper> possibleDependencyWrappers = getLibrariesWithMissingJavadocOrSourcesWrappers(PROJECT);
			for (final LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper : possibleDependencyWrappers) {
				chosenLibraries.add(possibleDependencyWrapper);
				// HIDE APPROXIMATIVE MATCHES IF SPECIFIED IN PREFERENCES, AND PRINT A MESSAGE INSTEAD
				// createClasspathEntryLabel(composite, possibleDependencyWrapper);
				// createSatisfiedLabels(composite, possibleDependencyWrapper);
			}
		} catch (JavaModelException e) {
			ErrorDialog errorDialog = new ErrorDialog(WizardsMessages.DependenciesFromClasspathPage_error_parsing_title, WizardsMessages.DependenciesFromClasspathPage_unexpected_error, e);
			errorDialog.open();
		} catch (Exception e) {
			ErrorDialog errorDialog = new ErrorDialog(WizardsMessages.DependenciesFromClasspathPage_error_parsing_title, WizardsMessages.DependenciesFromClasspathPage_unexpected_error, e);
			errorDialog.open();
		}
		// if (chosenLibraries.size() == 0) {
		// WizardContentsHelper.createDescriptionLabel(composite, WizardsMessages.DependenciesFromClasspathPage_noAvailableLibrary);
		// }
		// return composite;
	}

	/**
	 * Creates the satisfied labels.
	 * 
	 * @param composite
	 *            the composite
	 * @param possibleDependencyWrapper
	 *            the possible dependency wrapper
	 */
	// private void createSatisfiedLabels(Composite composite, LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper) {
	// final Label label = new Label(composite, SWT.FLAT | SWT.BORDER);
	// GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
	// layoutData.horizontalSpan = 1;
	// label.setLayoutData(layoutData);
	// StringBuilder builder = new StringBuilder();
	// if (possibleDependencyWrapper.getMissesJavadoc()) {
	// builder.append("No Javadoc associated with build path entry. ");
	// boolean javadocAvailable = false;
	// for (ArtifactVersionWrapper artifactVersionWrapper : possibleDependencyWrapper.getArtifactVersionWrappers()) {
	// if (artifactVersionWrapper.getArtifactVersion().getJavadocUrl() != null) {
	// javadocAvailable = true;
	// break;
	// }
	// }
	// if (javadocAvailable) {
	// builder.append("Javadoc available in repositories.");
	// } else {
	// builder.append("Not available in repositories.");
	// }
	// }
	// if (possibleDependencyWrapper.getMissesSources()) {
	// if (builder.length() > 0) {
	// builder.append("\n");
	// }
	// builder.append("No Sources associated with build path entry. ");
	// boolean sourcesAvailable = false;
	// for (ArtifactVersionWrapper artifactVersionWrapper : possibleDependencyWrapper.getArtifactVersionWrappers()) {
	// if (artifactVersionWrapper.getArtifactVersion().getSourcesUrl() != null && artifactVersionWrapper.getExactMatch()) {
	// sourcesAvailable = true;
	// break;
	// }
	// }
	// if (sourcesAvailable) {
	// builder.append("Sources available in repositories.");
	// } else {
	// builder.append("Not available in repositories.");
	// }
	// }
	// label.setText(builder.toString());
	// }
	/**
	 * Creates the classpath entry label.
	 * 
	 * @param composite
	 *            the composite
	 * @param possibleDependencyWrapper
	 *            the possible dependency wrapper
	 */
	// private void createClasspathEntryLabel(final Composite composite, final LibraryWithMissingJavadocOrSourcesWrapper possibleDependencyWrapper) {
	// final Label label = new Label(composite, SWT.FLAT | SWT.BORDER);
	// GridData layoutData = new GridData(SWT.FILL, SWT.NONE, true, false);
	// layoutData.horizontalSpan = 1;
	// label.setLayoutData(layoutData);
	// int backGroundColor = SWT.COLOR_DARK_GRAY;
	// if (possibleDependencyWrapper.getMissesSources() && !possibleDependencyWrapper.getMissesJavadoc()) {
	// backGroundColor = SWT.COLOR_DARK_BLUE;
	// }
	// if (possibleDependencyWrapper.getMissesSources() && possibleDependencyWrapper.getMissesJavadoc()) {
	// backGroundColor = SWT.COLOR_DARK_GREEN;
	// }
	// if (!possibleDependencyWrapper.getMissesSources() && possibleDependencyWrapper.getMissesJavadoc()) {
	// backGroundColor = SWT.COLOR_DARK_YELLOW;
	// }
	// label.setBackground(getShell().getDisplay().getSystemColor(backGroundColor));
	// label.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
	// label.setText(possibleDependencyWrapper.getLibraryId() + " " + (possibleDependencyWrapper.getLibraryPath())); //$NON-NLS-1$
	// }
	/**
	 * Gets the libraries with missing javadoc or sources wrappers.
	 * 
	 * @param javaProject
	 *            the java project
	 * 
	 * @return the libraries with missing javadoc or sources wrappers
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	private Set<LibraryWithMissingJavadocOrSourcesWrapper> getLibrariesWithMissingJavadocOrSourcesWrappers(IJavaProject javaProject) throws JavaModelException {
		Set<IPackageFragmentRoot> packageFragmentRoots = new TreeSet<IPackageFragmentRoot>(new Comparator<IPackageFragmentRoot>() {
			public int compare(IPackageFragmentRoot o1, IPackageFragmentRoot o2) {
				return o1.getElementName().compareTo(o2.getElementName());
			}

		});
		for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
			if (isLibraryWithMissingJavadocOrSource(packageFragmentRoot)) {
				packageFragmentRoots.add(packageFragmentRoot);
			}
		}
		Set<LibraryWithMissingJavadocOrSourcesWrapper> librariesWithMissingJavadocOrSources = new TreeSet<LibraryWithMissingJavadocOrSourcesWrapper>(new Comparator<LibraryWithMissingJavadocOrSourcesWrapper>() {

			public int compare(LibraryWithMissingJavadocOrSourcesWrapper o1, LibraryWithMissingJavadocOrSourcesWrapper o2) {
				return o1.getLibraryId().compareTo(o2.getLibraryId());
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
				if (hasExactMatch) {
					librariesWithMissingJavadocOrSources.add(new LibraryWithMissingJavadocOrSourcesWrapper(libraryPath, libraryId, artifactVersionWrappers, missesJavadoc(packageFragmentRoot), missesSources(packageFragmentRoot), packageFragmentRoot));
				}
			}
		}
		librariesWithMissingJavadocOrSources = Collections.unmodifiableSet(librariesWithMissingJavadocOrSources);
		return librariesWithMissingJavadocOrSources;
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
	 * Checks if is library with missing javadoc or source.
	 * 
	 * @param packageFragmentRoot
	 *            the package fragment root
	 * 
	 * @return the boolean
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	private Boolean isLibraryWithMissingJavadocOrSource(IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {
		boolean isArchive = packageFragmentRoot.isArchive();
		int entryKind = packageFragmentRoot.getRawClasspathEntry().getEntryKind();
		boolean isFiltered = isFiltered(packageFragmentRoot);
		boolean missesSources = missesSources(packageFragmentRoot);
		boolean missesJavadoc = missesJavadoc(packageFragmentRoot);
		return isArchive && (entryKind != IClasspathEntry.CPE_PROJECT) && entryKind != IClasspathEntry.CPE_SOURCE && !isFiltered && (missesJavadoc || missesSources);
	}

	/**
	 * Misses javadoc.
	 * 
	 * @param packageFragmentRoot
	 *            the package fragment root
	 * 
	 * @return true, if successful
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	private boolean missesJavadoc(IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {
		int entryKind = packageFragmentRoot.getRawClasspathEntry().getEntryKind();
		boolean missesJavadoc = false;
		if (entryKind == IClasspathEntry.CPE_LIBRARY || entryKind == IClasspathEntry.CPE_VARIABLE) {
			missesJavadoc = JavaUI.getLibraryJavadocLocation(packageFragmentRoot.getRawClasspathEntry()) == null;
		}
		if (entryKind == IClasspathEntry.CPE_CONTAINER) {
			missesJavadoc = JavaUI.getJavadocLocation(packageFragmentRoot.getPrimaryElement(), false) == null;
		}
		return missesJavadoc;
	}

	/**
	 * Misses sources.
	 * 
	 * @param packageFragmentRoot
	 *            the package fragment root
	 * 
	 * @return true, if successful
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	private boolean missesSources(IPackageFragmentRoot packageFragmentRoot) throws JavaModelException {
		return packageFragmentRoot.getSourceAttachmentPath() == null;
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
		Set<ArtifactVersion> resultList = new TreeSet<ArtifactVersion>(new Comparator<ArtifactVersion>() {

			public int compare(ArtifactVersion o1, ArtifactVersion o2) {
				int result = 0;
				Artifact artifact1 = (o1.getParent() == null) ? null : o1.getParent();
				Group group1 = (artifact1.getParent() == null) ? null : artifact1.getParent();
				Artifact artifact2 = (o2.getParent() == null) ? null : o2.getParent();
				Group group2 = (artifact2.getParent() == null) ? null : artifact2.getParent();
				int groupsComparison = group1 == null ? 0 : group1.compareTo(group2);
				if (groupsComparison == 0) {
					int artifactComparison = artifact1 == null ? 0 : artifact1.compareTo(artifact2);
					if (artifactComparison == 0) {
						int artifactVersionComparison = o1 == null ? 0 : o1.compareTo(o2);
						if (artifactVersionComparison == 0) {
							result = 1;
						} else {
							result = -artifactVersionComparison;
						}
					} else {
						result = artifactComparison;
					}
				} else {
					result = groupsComparison;
				}
				return result;
			}

		});
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
		if (gotJavadocExpandItem != null)
			gotJavadocExpandItem.setExpanded(visible);
		if (gotSourcesExpandItem != null)
			gotSourcesExpandItem.setExpanded(visible);
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
	public Set<LibraryWithMissingJavadocOrSourcesWrapper> getChosenDependencies() {
		return chosenLibraries;
	}

}