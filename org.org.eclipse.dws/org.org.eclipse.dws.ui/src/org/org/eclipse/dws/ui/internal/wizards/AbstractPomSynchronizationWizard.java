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
package org.org.eclipse.dws.ui.internal.wizards;

import java.util.Set;

import org.apache.log4j.Logger;
import org.org.eclipse.core.utils.platform.wizards.AbstractWizard;
import org.org.eclipse.dws.core.internal.bridges.WorkspaceInteractionHelper;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;

/**
 * The Class AbstractPomSynchronizationWizard.
 */
public abstract class AbstractPomSynchronizationWizard extends AbstractWizard {

	/** The logger. */
	protected static Logger logger = Logger.getLogger(AbstractPomSynchronizationWizard.class);

	/** The LIBRARIES to sync. */
	private final Set<AbstractChosenArtifactVersion> LIBRARIES;

	/** The PROJECT_NAMES. */
	private final String[] PROJECT_NAMES;

	/** The CHOSENPROJECT. */
	private String CHOSENPROJECT;

	/** The FILTERING OPTIONS. */
	private final PomDependenciesFilteringOptions FILTERING_OPTIONS;

	/** The PARSED POM DESCRIPTION. */
	private final Pom PARSED_POM_DESCRIPTION;

	/**
	 * Instantiates a new abstract pom synchronization wizard.
	 * 
	 * @param options the options
	 * @param pom the parsed pom description
	 * @param windowTitle the window title
	 * @param needsProgressMonitor the needs progress monitor
	 * @param natureIds the nature ids
	 */
	public AbstractPomSynchronizationWizard(PomDependenciesFilteringOptions options, Pom pom, String windowTitle, boolean needsProgressMonitor, String... natureIds) {
		super();
		this.FILTERING_OPTIONS = options;
		this.PARSED_POM_DESCRIPTION = pom;
		this.CHOSENPROJECT = null;
		this.LIBRARIES = getArtifactVersions(options, pom);
		this.PROJECT_NAMES = WorkspaceInteractionHelper.computeProjectNames(natureIds);
		setWindowTitle(windowTitle);
		setNeedsProgressMonitor(needsProgressMonitor);
	}

	/**
	 * Instantiates a new abstract pom synchronization wizard.
	 * 
	 * @param artifactVersions the artifact versions
	 * @param windowTitle the window title
	 * @param needsProgressMonitor the needs progress monitor
	 * @param natureIds the nature ids
	 */
	public AbstractPomSynchronizationWizard(Set<AbstractChosenArtifactVersion> artifactVersions, String windowTitle, boolean needsProgressMonitor, String... natureIds) {
		super();
		this.FILTERING_OPTIONS = null;
		this.PARSED_POM_DESCRIPTION = null;
		this.CHOSENPROJECT = null;
		this.LIBRARIES = artifactVersions;
		this.PROJECT_NAMES = WorkspaceInteractionHelper.computeProjectNames(natureIds);
		setWindowTitle(windowTitle);
		setNeedsProgressMonitor(needsProgressMonitor);
	}

	/**
	 * Gets the artifact versions.
	 * 
	 * @param options the options
	 * @param pom the parsed pom description
	 * 
	 * @return the artifact versions
	 */
	// FIXME this should be implemented on top of the Wizards, therefore removing the need for this class
	protected abstract Set<AbstractChosenArtifactVersion> getArtifactVersions(PomDependenciesFilteringOptions options, Pom pom);

	/**
	 * Gets the cHOSENPROJECT.
	 * 
	 * @return the cHOSENPROJECT
	 */
	public String getCHOSENPROJECT() {
		return CHOSENPROJECT;
	}

	/**
	 * Gets the lIBRARIES.
	 * 
	 * @return the lIBRARIES
	 */
	public Set<AbstractChosenArtifactVersion> getLIBRARIES() {
		return LIBRARIES;
	}

	/**
	 * Gets the pROJEC t_ names.
	 * 
	 * @return the pROJEC t_ names
	 */
	public String[] getPROJECT_NAMES() {
		return PROJECT_NAMES;
	}

	/**
	 * Sets the cHOSENPROJECT.
	 * 
	 * @param chosenproject the new cHOSENPROJECT
	 */
	public void setCHOSENPROJECT(String chosenproject) {
		CHOSENPROJECT = chosenproject;
	}

	/**
	 * Gets the fILTERIN g_ options.
	 * 
	 * @return the fILTERIN g_ options
	 */
	public PomDependenciesFilteringOptions getFILTERING_OPTIONS() {
		return FILTERING_OPTIONS;
	}

	/**
	 * Gets the parses the d_ po m_ description.
	 * 
	 * @return the parses the d_ po m_ description
	 */
	public Pom getPARSED_POM_DESCRIPTION() {
		return PARSED_POM_DESCRIPTION;
	}
}
