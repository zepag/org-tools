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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.ArtifactVersion;

/**
 * The Class AbstractDWSViewAction.
 */
public abstract class AbstractDWSViewAction extends AbstractSimpleAction {

	/** The resolver. */
	private IActionResolver resolver;

	/** The action host. */
	private IActionHost actionHost;

	/**
	 * Instantiates a new abstract download action.
	 * 
	 * @param actionHost
	 *            the action host
	 */
	public AbstractDWSViewAction(IActionHost actionHost) {
		this.actionHost = actionHost;
	}

	/**
	 * Sets the resolver.
	 * 
	 * @param resolver
	 *            the new resolver
	 */
	public void setResolver(IActionResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		boolean result = false;
		if (resolver == null) {
			result = false;
		} else {
			result = resolver.isEnabled();
		}
		return result;
	}

	/**
	 * Gets the action host.
	 * 
	 * @return the action host
	 */
	public IActionHost getActionHost() {
		return actionHost;
	}

	@SuppressWarnings("rawtypes")
	protected List<ArtifactVersion> retrieveArtifactVersions(IStructuredSelection selection) {
		Set<ArtifactVersion> artifactVersion = new HashSet<ArtifactVersion>();
		for (Object item : selection.toList()) {
			if (item instanceof IModelItem) {
				ArtifactsRetrievalVisitor artifactsRetrievalVisitor = new ArtifactsRetrievalVisitor();
				((IModelItem) item).accept(artifactsRetrievalVisitor);
				artifactVersion.addAll(artifactsRetrievalVisitor.getArtifactVersions());
			}
		}
		return new ArrayList<ArtifactVersion>(artifactVersion);
	}
}