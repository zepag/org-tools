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
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.dws.core.internal.dialogs.UrlInputDialog;
import org.org.eclipse.dws.core.internal.jobs.ImportRepositoryFromUrlJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;


/**
 * The Class ImportRepositoryFromUrlHandler.
 */
public class ImportRepositoryFromUrlHandler extends AbstractExtendedHandler<Object> {
	
	/** The Constant COMMAND_ID. */
	private static final String COMMAND_ID = "org.org.eclipse.dws.core.importRepositoryFromUrlCommand";
	
	/** The Constant URL. */
	private static final String URL = COMMAND_ID + ".url";

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		String url = event.getParameter(URL);
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			url = null;
		}
		if (url == null) {
			UrlInputDialog fileDialog = new UrlInputDialog(Display.getCurrent().getActiveShell());
			fileDialog.setValidator(new IFieldsValidator() {

				@SuppressWarnings("rawtypes")
				public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
					StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
					String url = (String) fieldValueHolders.get(UrlInputDialog.URL_FIELD).getValue();
					if (url.trim().length() == 0) {
						validationResult.append("Url should not be empty\n");
					} else {
						try {
							URL urlObj = new URL(url);
							Proxy proxy = IOToolBox.determineProxy(urlObj);
							urlObj.openConnection(proxy).getInputStream();
						} catch (MalformedURLException e) {
							validationResult.append("Malformed Url:\n\t" + e.getMessage());
						} catch (IOException e) {
							validationResult.append("Impossible to reach Url:\n\t" + e.getMessage() + "\nTry changing Eclipse's proxy settings if needed. ");
						} catch (URISyntaxException e) {
							validationResult.append("URI Syntax issue:\n\t" + e.getMessage());
						}
					}
					return validationResult;
				}

			});
			if (fileDialog.open() == Window.OK) {
				url = fileDialog.getUrl();
			}
		}
		if (url != null) {
			Job job = new ImportRepositoryFromUrlJob(url);
			job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notificaiton", "Import repositories definition from Url:\n"));
			job.schedule();
		}
		return null;
	}
}
