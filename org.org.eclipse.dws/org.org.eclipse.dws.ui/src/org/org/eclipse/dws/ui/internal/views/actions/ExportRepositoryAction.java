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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.eclipse.dws.core.internal.bridges.RepositoryModelPersistence;
import org.org.repository.crawler.maven2.model.CrawledRepository;


/**
 * The Class ExportRepositoryAction.
 * 
 * @author pagregoire
 */
public class ExportRepositoryAction extends AbstractSimpleAction {

	/** The resolver. */
	private IActionResolver resolver;

	/** The action host. */
	private IActionHost actionHost;

	/**
	 * Instantiates a new export repository action.
	 * 
	 * @param actionHost the action host
	 */
	public ExportRepositoryAction(IActionHost actionHost) {
		this.actionHost = actionHost;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
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
	 * Sets the resolver.
	 * 
	 * @param resolver the new resolver
	 */
	public void setResolver(IActionResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) actionHost
				.getActionTrigger();
		Set<CrawledRepository> crawledRepositories = new HashSet<CrawledRepository>();
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object next = it.next();
			if (next instanceof CrawledRepository) {
				CrawledRepository crawledRepository = (CrawledRepository) next;
				crawledRepositories.add(crawledRepository);
			}
		}
		try {
			FileDialog fileDialog = new FileDialog(Display.getCurrent()
					.getActiveShell());
			fileDialog.setFileName("maven2-repo-export.xml");
			fileDialog.setFilterExtensions(new String[] { "*.xml" });
			fileDialog.setText("Export");

			String fileName = fileDialog.open();
			if (fileName == null) {
				return;
			}
			File tmpFile = new File(fileName);
			RepositoryModelPersistence.exportRepositoryInfo(crawledRepositories, tmpFile);

		} catch (Throwable e) {
			ErrorDialog errorDialog = new ErrorDialog(
					"Problem while exporting", "Could not write file", e);
			errorDialog.open();
		}

	}
}