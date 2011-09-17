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
import org.eclipse.ui.IViewPart;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.dws.ui.internal.views.DetailsView;
import org.org.eclipse.dws.ui.internal.views.MavenRepositoriesView;
import org.org.model.IModelItem;


/**
 * The Class ShowDetailsAction.
 * 
 * @author pagregoire
 */
public class ShowDetailsAction extends AbstractDWSViewAction {

	/**
	 * Instantiates a new show details action.
	 * 
	 * @param actionHost the action host
	 */
	public ShowDetailsAction(IActionHost actionHost) {
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
		if (selection.getFirstElement() instanceof IModelItem) {
			IViewPart detailsViewHandle=DetailsView.showView();
			if(detailsViewHandle!=null){
				MavenRepositoriesView.showViewAndFocusOnElement((IModelItem) selection.getFirstElement());
				detailsViewHandle.getSite().getPage().activate(detailsViewHandle);
			}
		}
	}
}
