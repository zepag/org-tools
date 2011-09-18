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

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.dws.core.internal.dialogs.VariableChangePromptDialog;
import org.org.eclipse.dws.core.internal.jobs.ChangeClasspathVariableJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;

/**
 * The Class ChangeClasspathVariableHandler.
 */
public class ChangeClasspathVariableHandler extends AbstractExtendedHandler<Object> {

	/** The Constant ORIGINAL_VARIABLE_PARAMETER. */
	public static final String ORIGINAL_VARIABLE_PARAMETER = "org.org.eclipse.dws.core.changeClasspathVariableCommand.originalVariable";

	/** The Constant TARGET_VARIABLE_PARAMETER. */
	public static final String TARGET_VARIABLE_PARAMETER = "org.org.eclipse.dws.core.changeClasspathVariableCommand.targetVariable";

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		String originalVariable = event.getParameter(ORIGINAL_VARIABLE_PARAMETER);
		String targetVariable = event.getParameter(TARGET_VARIABLE_PARAMETER);
		if (originalVariable == null || targetVariable == null) {
			VariableChangePromptDialog variableChangePromptDialog = new VariableChangePromptDialog(HandlerUtil.getActiveShell(event));
			variableChangePromptDialog.setValidator(new IFieldsValidator() {

				@SuppressWarnings("rawtypes")
				public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
					StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
					String original = (String) fieldValueHolders.get(VariableChangePromptDialog.ORIGINAL_FIELD).getValue();
					if (original == null || !original.matches("[A-Za-z0-9_]+")) {
						validationResult.append("Variable should be defined and be alpha-numeric (underscore allowed)");
					}
					String target = (String) fieldValueHolders.get(VariableChangePromptDialog.TARGET_FIELD).getValue();
					if (target == null || !target.matches("[A-Za-z0-9_]+")) {
						validationResult.append("Target should be defined and be alpha-numeric (underscore allowed)");
					}
					return validationResult;
				}

			});
			if (variableChangePromptDialog.open() == Window.OK) {
				originalVariable = variableChangePromptDialog.getOriginalVariable();
				targetVariable = variableChangePromptDialog.getTargetVariable();
			}
		}
		if (originalVariable != null && targetVariable != null) {
			Job job = new ChangeClasspathVariableJob(originalVariable, targetVariable);
			job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification", "Variables change ended:\n"));
			job.schedule();
		}
		return null;
	}
}