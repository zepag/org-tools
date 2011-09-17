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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.cheatsheet.commands.internal.jobs.CreateFileInFileSystemJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.SimpleFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog.FieldType;
import org.org.eclipse.core.utils.platform.filegen.dialogs.FieldDescriptorsBasedPromptDialog;
import org.org.eclipse.core.utils.platform.filegen.dialogs.FieldDescriptorsValidator;
import org.org.eclipse.core.utils.platform.filegen.dialogs.FieldDescriptorsBasedPromptDialog.FieldDescriptor;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class CreateFileInFileSystemFromUrlHandler extends AbstractExtendedHandler<Object> {
	private static final String COMMAND_ID = "org.org.eclipse.cheatsheet.commands.createFileInFileSystemFromUrlCommand";
	private static final String FILE_URL_PARAMETER = COMMAND_ID + ".fileUrl";
	private static final String TARGET_FOLDER_PARAMETER = COMMAND_ID + ".targetPath";
	private static final String MODE_PARAMETER = COMMAND_ID + ".mode";
	private static final String TARGET_FILE_NAME_PARAMETER = COMMAND_ID + ".targetFileName";
	private static final String CUSTOM_SUFFIX_PARAMETER = COMMAND_ID + ".customSuffix";

	/**
	 * The constructor.
	 */
	public CreateFileInFileSystemFromUrlHandler() {
	}

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext savedContext) throws ExtendedHandlerExecutionException {
		URL fileUrl = null;
		try {
			fileUrl = new URL(event.getParameter(FILE_URL_PARAMETER));
		} catch (MalformedURLException e) {
			throw new ExtendedHandlerExecutionException("Impossible to parse file's URL", e);
		}
		String mode = event.getParameter(MODE_PARAMETER);
		if (mode == null)
			mode = ModeParameterValues.REPLACE;
		String targetPath = event.getParameter(TARGET_FOLDER_PARAMETER);
		if (targetPath == null)
			targetPath = System.getProperty("user.home");
		String targetFileName = event.getParameter(TARGET_FILE_NAME_PARAMETER);
		if (targetFileName == null)
			targetFileName = determineFileNameFrom(fileUrl);
		if (targetFileName == null)
			throw new ExtendedHandlerExecutionException("Invalid URL, not pointing to a file.");
		String customSuffix = event.getParameter(CUSTOM_SUFFIX_PARAMETER);
		if (mode == null) {
			mode = ModeParameterValues.REPLACE;
		}
		boolean prompt = mode.equals(ModeParameterValues.PROMPT);
		// LocalFileCreationPromptDialog userPromptDialog = new LocalFileCreationPromptDialog(HandlerUtil.getActiveShell(event), targetPath, prompt);
		List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
		// BEGIN: This should be enabled by a preference
		fieldDescriptors.add(new FieldDescriptor("remote.url", "Remote file", fileUrl.toString(), true, FieldType.TEXT, true));
		if (customSuffix != null) {
			fieldDescriptors.add(new FieldDescriptor("custom.suffix", "Custom suffix", customSuffix, false, FieldType.TEXT, true));
		}
		// END: This should be enabled by a preference
		fieldDescriptors.add(new FieldDescriptor("chosen.path", "Path *", targetPath, true, FieldType.FOLDER_CHOICE, true));
		fieldDescriptors.add(new FieldDescriptor("target.fileName", "Target file name *", targetFileName, true, FieldType.TEXT, false));
		if (prompt) {
			String[] modes = new String[] { ModeParameterValues.REPLACE, ModeParameterValues.SKIP, ModeParameterValues.SUFFIX };
			FieldDescriptor modesFieldDescriptor = new FieldDescriptor("chosen.mode", "Path *", mode, true, FieldType.SINGLE_CHOICE_COMBO);
			modesFieldDescriptor.setPossibleValues(modes);
			fieldDescriptors.add(modesFieldDescriptor);
		}
		FieldDescriptorsBasedPromptDialog userPromptDialog = new FieldDescriptorsBasedPromptDialog(HandlerUtil.getActiveShell(event), "Local File creation prompt", fieldDescriptors);
		userPromptDialog.setValidator(new FieldDescriptorsValidator(fieldDescriptors) {

			@SuppressWarnings("unchecked")
			@Override
			public IValidationResult additionalValidate(IValidationResult result, Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders, List<FieldDescriptor> fieldDescriptors) {
				String chosenPath = (String) fieldValueHolders.get(new SimpleFieldIdentifier("chosen.path")).getValue();
				if (!(IOToolBox.fileExists(new File(chosenPath)))) {
					result.appendMessage("Chosen path does not exist on the filesystem.");
				}
				if (!(IOToolBox.fileWriteable(new File(chosenPath)))) {
					result.appendMessage("Chosen path is not writeable.");
				}
				IFieldValueHolder modeFieldValueHolder = fieldValueHolders.get(new SimpleFieldIdentifier("chosen.mode"));
				if (modeFieldValueHolder != null) {
					String mode = (String) modeFieldValueHolder.getValue();
					if (mode == null || mode.trim().equals("")) {
						modeFieldValueHolder.setValue(ModeParameterValues.SUFFIX);
					}
				}
				return result;
			}

		});
		if (userPromptDialog.open() == Window.OK) {
			String chosenPath = userPromptDialog.getFieldValue("chosen.path");
			mode = mode.equals(ModeParameterValues.PROMPT) ? userPromptDialog.getFieldValue("chosen.mode") : mode;
			try {
				fileUrl = new URL(userPromptDialog.getFieldValue("remote.url"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			targetFileName = userPromptDialog.getFieldValue("target.fileName");
			customSuffix = userPromptDialog.getFieldValue("custom.suffix");
			launchJob(fileUrl, mode, targetPath, targetFileName, customSuffix, chosenPath);
		}
		return null;
	}

	protected void launchJob(URL fileUrl, String mode, String targetFolder, String targetFileName, String customSuffix, String targetPath) {
		Job job = new CreateFileInFileSystemJob(targetPath, fileUrl, targetFileName, mode, customSuffix);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification", "Creation of file in file system:\n"));
		job.schedule();
	}

	private String determineFileNameFrom(URL fileUrl) {
		StringTokenizer tkz = new StringTokenizer(fileUrl.toExternalForm(), "\\/", true);
		String result = null;
		while (tkz.hasMoreTokens()) {
			result = tkz.nextToken();

		}
		if (result != null && !result.matches(".*\\..*")) {
			result = null;
		}
		return result;
	}
}
