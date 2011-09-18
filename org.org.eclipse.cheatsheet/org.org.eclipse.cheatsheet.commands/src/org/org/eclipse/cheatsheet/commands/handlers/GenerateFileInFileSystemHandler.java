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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.cheatsheet.commands.internal.jobs.GenerateFileInFileSystemJob;
import org.org.eclipse.core.ui.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.AbstractInputTrayDialog.FieldType;
import org.org.eclipse.core.utils.platform.filegen.IFileGenerator;
import org.org.eclipse.core.utils.platform.filegen.TemplateEngine;
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
public class GenerateFileInFileSystemHandler extends AbstractExtendedHandler<Object> {
	private static final String COMMAND_ID = "org.org.eclipse.cheatsheet.commands.generateFileInFileSystemCommand";
	private static final String TEMPLATE_ENGINE_PARAMETER = COMMAND_ID + ".templateEngine";
	private static final String TEMPLATE_URL_PARAMETER = COMMAND_ID + ".templateUrl";
	private static final String PARAMETERS_URL_PARAMETER = COMMAND_ID + ".templateParametersUrl";

	/**
	 * The constructor.
	 */
	public GenerateFileInFileSystemHandler() {
	}

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext savedContext) throws ExtendedHandlerExecutionException {
		String templateEngine = event.getParameter(TEMPLATE_ENGINE_PARAMETER);
		if (templateEngine == null) {
			throw new ExtendedHandlerExecutionException("Invalid CheatSheet command, template engine should be defined.");
		}
		URL templateUrl = null;
		try {
			templateUrl = new URL(event.getParameter(TEMPLATE_URL_PARAMETER));
		} catch (MalformedURLException e) {
			throw new ExtendedHandlerExecutionException("Impossible to parse template's URL", e);
		}
		URL templateParametersUrl = null;
		try {
			templateParametersUrl = new URL(event.getParameter(PARAMETERS_URL_PARAMETER));
		} catch (MalformedURLException e) {
			throw new ExtendedHandlerExecutionException("Impossible to parse template parameters' URL", e);
		}

		Properties templateParameters = new Properties();
		InputStream inputStream = null;
		try {
			inputStream = templateParametersUrl.openConnection(IOToolBox.determineProxy(templateParametersUrl)).getInputStream();
			templateParameters.load(inputStream);
		} catch (IOException e) {
			throw new ExtendedHandlerExecutionException("Problem occured while loading generation properties", e);
		} catch (URISyntaxException e) {
			throw new ExtendedHandlerExecutionException("Problem occured while loading generation properties", e);
		} finally {
			try {
				inputStream.close();
			} catch (Throwable e) {
			}
		}
		List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>();
		// BEGIN: This should be enabled by a preference
		fieldDescriptors.add(new FieldDescriptor("template.engine", "Template engine *", templateEngine, true, FieldType.TEXT, true));
		fieldDescriptors.add(new FieldDescriptor("template.url", "Template url *", templateUrl.toString(), true, FieldType.TEXT, true));
		fieldDescriptors.add(new FieldDescriptor("template.parameters.url", "Template parameters url *", templateParametersUrl.toString(), true, FieldType.TEXT, true));
		// END: This should be enabled by a preference
		String defaultTargetFileName = "default.txt";
		for (Object keyObj : templateParameters.keySet()) {
			String key = (String) keyObj;
			// properties with .label, .mandatory, .type suffix are not retained, so are not the mandatory target fields.
			if (key.equals(IFileGenerator.TARGET_FILENAME_PROPERTY)) {
				defaultTargetFileName = templateParameters.getProperty(key);
			}
			if (!key.endsWith(".label") && !key.endsWith(".mandatory") && !key.equals(IFileGenerator.TARGET_PATH_PROPERTY)) {
				Boolean propertyMandatory = Boolean.valueOf(templateParameters.getProperty(key + ".mandatory"));
				String propertyTypeStr = templateParameters.getProperty(key + ".type");
				FieldType propertyType = propertyTypeStr == null ? FieldType.TEXT : FieldType.valueOf(propertyTypeStr);
				String propertyLabel = templateParameters.getProperty(key + ".label");
				propertyLabel = propertyLabel == null ? "[" + key + "]" : propertyLabel;
				propertyLabel += propertyMandatory ? " *" : "";
				FieldDescriptor fieldDescriptor = new FieldDescriptor(key, propertyLabel, templateParameters.getProperty(key), propertyMandatory, propertyType);
				fieldDescriptors.add(fieldDescriptor);
			}
		}
		fieldDescriptors.add(new FieldDescriptor(IFileGenerator.TARGET_PATH_PROPERTY, "Target Path *", System.getProperty("user.home"), true, FieldType.FOLDER_CHOICE));
		fieldDescriptors.add(new FieldDescriptor(IFileGenerator.TARGET_FILENAME_PROPERTY, "Target File name *", defaultTargetFileName, true, FieldType.TEXT));
		FieldDescriptorsBasedPromptDialog dialog = new FieldDescriptorsBasedPromptDialog(HandlerUtil.getActiveShell(event), "Template properties prompt", fieldDescriptors);
		dialog.setValidator(new FieldDescriptorsValidator(fieldDescriptors) {

			
			@SuppressWarnings("rawtypes")
			@Override
			public IValidationResult additionalValidate(IValidationResult result, Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders, List<FieldDescriptor> fieldDescriptors) {
				return result;
			}

		});
		if (dialog.open() == Window.OK) {
			String targetFileName = "default.txt";
			String targetPath = System.getProperty("user.home");
			Map<String, IFieldValueHolder<String>> fieldValueHolders = dialog.getFieldValueHolders();
			Map<String, String> resolvedFields = new HashMap<String, String>();
			for (String key : fieldValueHolders.keySet()) {
				if (key.equals(IFileGenerator.TARGET_PATH_PROPERTY)) {
					targetPath = fieldValueHolders.get(key).getValue();
				} else if (key.equals(IFileGenerator.TARGET_FILENAME_PROPERTY)) {
					targetFileName = fieldValueHolders.get(key).getValue();
				} else {
					resolvedFields.put(key, fieldValueHolders.get(key).getValue());
				}
			}
			String targetFile = targetPath + pathSeparatorNeeded(targetPath) + targetFileName;
			launchJob(templateEngine, templateUrl, resolvedFields, targetFile);
		}

		return null;
	}

	private String pathSeparatorNeeded(String targetPath) {
		String result = "";
		if (!(targetPath.endsWith("/") || targetPath.endsWith("\\"))) {
			result = "/";
		}
		return result;
	}

	private void launchJob(String templateEngine, URL templateUrl, Map<String, String> resolvedFields, String targetFile) {
		Job job = new GenerateFileInFileSystemJob(TemplateEngine.VELOCITY, templateUrl, resolvedFields, targetFile);
		job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG CheatSheet Helpers Notification", "Generation of file in file system:\n"));
		job.schedule();
	}

}
