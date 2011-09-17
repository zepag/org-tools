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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.core.ui.dialogs.InfoDialog;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelUtils;
import org.org.repository.crawler.maven2.model.ArtifactVersion;


/**
 * The Class ListTransitiveDependenciesAction.
 * 
 * @author pagregoire
 */
public class ListTransitiveDependenciesAction extends AbstractDWSViewAction {

	/**
	 * Instantiates a new list transitive dependencies action.
	 * 
	 * @param actionHost the action host
	 */
	public ListTransitiveDependenciesAction(IActionHost actionHost) {
		super(actionHost);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
		Object obj = (selection).getFirstElement();
		if (obj instanceof ArtifactVersion) {
			ArtifactVersion artifactVersion = (ArtifactVersion) selection.getFirstElement();
			
			final StringBuilder buffer = RepositoryModelUtils.getTransitiveDependenciesDescription(artifactVersion);
			InfoDialog infoDialog = new InfoDialog(artifactVersion.getUID() + " ", buffer.toString());
			infoDialog.open();
		}
	}

}
