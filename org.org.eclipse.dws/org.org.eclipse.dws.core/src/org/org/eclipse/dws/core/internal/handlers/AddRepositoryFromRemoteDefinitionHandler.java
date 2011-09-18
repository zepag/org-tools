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
package org.org.eclipse.dws.core.internal.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.binding.BindingException;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.dws.core.internal.jobs.AddRepositoryJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.dws.core.internal.xml.IXmlRepositoriesBinder;
import org.org.eclipse.dws.core.internal.xml.StaxRepositoriesBinder;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AddRepositoryFromRemoteDefinitionHandler extends AbstractExtendedHandler<Object> {

	/** The Constant COMMAND_ID. */
	private static final String COMMAND_ID = "org.org.eclipse.dws.core.addRepositoryFromRemoteDefinitionCommand";

	/** The Constant DEFINITION_URL. */
	private static final String DEFINITION_URL = COMMAND_ID + ".definitionURL";

	/**
	 * Instantiates a new adds the repository from remote definition handler.
	 */
	public AddRepositoryFromRemoteDefinitionHandler() {
		super();
	}

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		URL fileUrl = null;
		try {
			fileUrl = new URL(event.getParameter(DEFINITION_URL));
		} catch (MalformedURLException e) {
			throw new ExtendedHandlerExecutionException("Impossible to parse file's URL", e);
		}
		IXmlRepositoriesBinder repositoriesBinder = new StaxRepositoriesBinder();
		CrawledRepository repository;
		try {
			repository = repositoriesBinder.parseXmlDefinition(fileUrl.openConnection(IOToolBox.determineProxy(fileUrl)).getInputStream());
		} catch (IOException e) {
			ErrorDialog errorDialog = new ErrorDialog("IO Error", "Impossible to open connection to file.", e);
			errorDialog.open();
			throw new ExtendedHandlerExecutionException("A problem occured while trying to add a repository", e);
		} catch (BindingException e) {
			ErrorDialog errorDialog = new ErrorDialog("XML Binding error", "Impossible to parse repository description.", e);
			errorDialog.open();
			throw new ExtendedHandlerExecutionException("A problem occured while trying to add a repository.", e);
		} catch (URISyntaxException e) {
			ErrorDialog errorDialog = new ErrorDialog("URI Syntax Error", "Impossible to open connection to file.", e);
			errorDialog.open();
			throw new ExtendedHandlerExecutionException("A problem occured while trying to add a repository", e);
		}
		Job job = new AddRepositoryJob(repository);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Job completion", "CrawledRepository successfully added: \n"));
		job.schedule();
		return null;
	}
}
