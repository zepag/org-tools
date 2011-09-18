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

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IViewPart;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.dws.ui.internal.search.LibraryNameQuery;
import org.org.eclipse.dws.ui.internal.search.LibrarySearchScope;



/**
 * The Class SearchLibAction.
 * 
 * @author pagregoire
 */
public class SearchLibAction extends AbstractSimpleAction {
	
	/** The selection. */
	private ISelection selection;

	/**
	 * Inits the.
	 * 
	 * @param view the view
	 */
	public void init(IViewPart view) {
	}

	/**
	 * Selection changed.
	 * 
	 * @param action the action
	 * @param selection the selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/**
	 * Run.
	 * 
	 * @param action the action
	 */
	public void run(IAction action) {
		run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		String className = getSelectedClassName();
		if (className != null) {
			LibrarySearchScope scope = LibrarySearchScope.newSearchScope();
			ISearchQuery query = new LibraryNameQuery(scope, className);
			NewSearchUI.activateSearchResultView();
			NewSearchUI.runQueryInBackground(query);
		}
	}

	/**
	 * Gets the selected class name.
	 * 
	 * @return the selected class name
	 */
	private String getSelectedClassName() {
		if ((selection instanceof IStructuredSelection) && !selection.isEmpty()) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj instanceof IType) {
				return ((IType) obj).getFullyQualifiedName();
			} else if (obj instanceof ICompilationUnit) {
				return ((ICompilationUnit) obj).findPrimaryType().getFullyQualifiedName();
			} else if (obj instanceof IClassFile) {
				try {
					return ((IClassFile) obj).getType().getFullyQualifiedName();
				} catch (Exception e) {
					// Can't do nothing here
				}
			}
		}
		return null;
	}
}
