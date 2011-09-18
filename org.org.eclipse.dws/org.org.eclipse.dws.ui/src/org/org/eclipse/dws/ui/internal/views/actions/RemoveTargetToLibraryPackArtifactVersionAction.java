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
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPackArtifactVersion.Target;
import org.org.eclipse.dws.ui.internal.views.LibraryPacksView;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class CopyDependencyInfoAction.
 * 
 * @author pagregoire
 */
public class RemoveTargetToLibraryPackArtifactVersionAction extends AbstractDWSViewAction {
	private final Target target;

	/**
	 * The Constructor.
	 * 
	 * @param clipboard
	 *            the clipboard
	 * @param actionHost
	 *            the action host
	 */
	public RemoveTargetToLibraryPackArtifactVersionAction(Target target, IActionHost actionHost) {
		super(actionHost);
		this.target = target;
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
		List<ArtifactVersion> artifactVersions = retrieveArtifactVersions(selection);
		for (ArtifactVersion artifactVersion : artifactVersions) {
			LibraryPackArtifactVersion libraryPackArtifactVersion = (LibraryPackArtifactVersion) artifactVersion;
			Set<Target> targets = libraryPackArtifactVersion.getTargets();
			targets.remove(target);
			libraryPackArtifactVersion.setTargets(targets.toArray(new Target[] {}));
		}
		LibraryPacksView.refreshViewer();
	}
}
