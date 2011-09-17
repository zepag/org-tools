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
package org.org.eclipse.cheatsheet.commands.handlers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.cheatsheet.commands.internal.jobs.AddFileContentInClipboardJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog.FieldType;
import org.org.eclipse.core.utils.platform.filegen.dialogs.FieldDescriptorsBasedPromptDialog;
import org.org.eclipse.core.utils.platform.filegen.dialogs.FieldDescriptorsValidator;
import org.org.eclipse.core.utils.platform.filegen.dialogs.FieldDescriptorsBasedPromptDialog.FieldDescriptor;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AddFileContentToClipboardFromUrlHandler extends AbstractExtendedHandler<Object> {
	private static final String COMMAND_ID = "org.org.eclipse.cheatsheet.commands.addFileContentToClipboardFromUrlCommand";
	private static final String FILE_URL_PARAMETER = COMMAND_ID + ".fileUrl";
	private static final String ENCODING_PARAMETER = COMMAND_ID + ".encoding";

	/**
	 * The constructor.
	 */
	public AddFileContentToClipboardFromUrlHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information from the application context.
	 */
	public Object doExecute(ExecutionEvent event, IExecutionSavedContext savedContext) throws ExtendedHandlerExecutionException {
		URL fileUrl = null;
		try {
			fileUrl = new URL(event.getParameter(FILE_URL_PARAMETER));

			String encoding = event.getParameter(ENCODING_PARAMETER);
			if (encoding == null) {
				encoding = System.getProperty("file.encoding");
			}
			List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
			// BEGIN: This should be enabled by a preference
			fieldDescriptors.add(new FieldDescriptor("remote.url", "Remote file", fileUrl.toString(), true, FieldType.TEXT, true));
			fieldDescriptors.add(new FieldDescriptor("encoding", "Encoding", encoding, true, FieldType.TEXT, true));
			// END: This should be enabled by a preference
			FieldDescriptorsBasedPromptDialog userPromptDialog = new FieldDescriptorsBasedPromptDialog(HandlerUtil.getActiveShell(event), "Adding content to clipboard", fieldDescriptors);
			userPromptDialog.setValidator(new FieldDescriptorsValidator(fieldDescriptors) {

				@SuppressWarnings("unchecked")
				@Override
				public IValidationResult additionalValidate(IValidationResult result, Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders, List<FieldDescriptor> fieldDescriptors) {
					return result;
				}

			});
			if (userPromptDialog.open() == Window.OK) {
				launchJob(new URL(userPromptDialog.getFieldValue("remote.url")), userPromptDialog.getFieldValue("encoding"));
			}
		} catch (MalformedURLException e) {
			throw new ExtendedHandlerExecutionException("Impossible to parse file's URL", e);
		}
		return null;
	}

	protected void launchJob(URL fileUrl, String encoding) {
		Job job = new AddFileContentInClipboardJob(fileUrl, encoding);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification", "Creation of file in workspace:\n"));
		job.schedule();
	}
}
