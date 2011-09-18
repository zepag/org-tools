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
package org.org.eclipse.dws.ui.internal.views.actions;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.org.eclipse.core.ui.dialogs.WarningDialog;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectHelper;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.ui.internal.wizards.PomJavaClasspathSynchronizationWizard;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class DownloadAndAddToClasspathAction.
 * 
 * @author pagregoire
 */
public class DownloadAndAddToClasspathAction extends AbstractDWSViewAction {

	/**
	 * Instantiates a new download and add to classpath action.
	 * 
	 * @param actionHost
	 *            the action host
	 */
	public DownloadAndAddToClasspathAction(IActionHost actionHost) {
		super(actionHost);
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
		if (JavaProjectHelper.areThereJavaProjects()) {
			List<ArtifactVersion> list = retrieveArtifactVersions(selection);
			Set<AbstractChosenArtifactVersion> artifactVersions = RepositoryModelUtils.computeLibrariesFromArtifactVersions(list);
			PomJavaClasspathSynchronizationWizard libraryHandlingWizard = new PomJavaClasspathSynchronizationWizard(artifactVersions);
			WizardDialog dialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), libraryHandlingWizard);
			dialog.open();
		} else {
			WarningDialog warningDialog = new WarningDialog("Did not download ", "Could not find any project to add this library to.");
			warningDialog.open();
		}
	}
}
