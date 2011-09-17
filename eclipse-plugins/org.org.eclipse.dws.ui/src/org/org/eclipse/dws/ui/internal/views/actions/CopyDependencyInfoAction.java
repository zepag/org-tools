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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class CopyDependencyInfoAction.
 * 
 * @author pagregoire
 */
public class CopyDependencyInfoAction extends AbstractDWSViewAction {

	/** The clipboard. */
	private Clipboard clipboard;

	/**
	 * The Constructor.
	 * 
	 * @param clipboard
	 *            the clipboard
	 * @param actionHost
	 *            the action host
	 */
	public CopyDependencyInfoAction(IActionHost actionHost, Clipboard clipboard) {
		super(actionHost);
		this.clipboard = clipboard;
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) getActionHost().getActionTrigger();
		List<ArtifactVersion> artifactVersions = retrieveArtifactVersions(selection);
		StringBuilder builder = new StringBuilder();
		for (ArtifactVersion artifactVersion : artifactVersions) {
			builder.append(PomInteractionHelper.toDependencyXML(artifactVersion));
		}
		clipboard.setContents(new Object[] { builder.toString() }, new Transfer[] { TextTransfer.getInstance() });
	}

}