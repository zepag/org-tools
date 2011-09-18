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
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.repository.crawler.maven2.model.ArtifactVersion;


/**
 * The Class ShowDependencyInfoAction.
 * 
 * @author pagregoire
 */
public class ShowDependencyInfoAction extends AbstractDWSViewAction {

	/**
	 * Instantiates a new show dependency info action.
	 * 
	 * @param actionHost the action host
	 */
	public ShowDependencyInfoAction(IActionHost actionHost) {
		super(actionHost);
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
		Object obj = (selection).getFirstElement();
		if (obj instanceof ArtifactVersion) {
			ArtifactVersion artifactVersion = (ArtifactVersion) selection.getFirstElement();
			StringBuilder buffer = new StringBuilder(PomInteractionHelper.toDependencyXML(artifactVersion));
			InfoDialog infoDialog = new InfoDialog(artifactVersion.getUID() + " ", buffer.toString());
			infoDialog.open();
		}
	}
}
