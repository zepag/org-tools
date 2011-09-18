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

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectHelper;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.dws.core.internal.dialogs.ProjectPromptDialog;
import org.org.eclipse.dws.core.internal.jobs.AddMavenLibraryToClasspathJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;


/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AddMavenLibraryToClasspathHandler extends AbstractExtendedHandler<Object> {
	
	/** The Constant COMMAND_ID. */
	private static final String COMMAND_ID = "org.org.eclipse.dws.core.addMavenLibraryToClasspathCommand";
	
	/** The Constant GROUP. */
	private static final String GROUP = COMMAND_ID + ".group";
	
	/** The Constant ARTIFACT. */
	private static final String ARTIFACT = COMMAND_ID + ".artifact";
	
	/** The Constant VERSION. */
	private static final String VERSION = COMMAND_ID + ".version";
	
	/** The Constant CLASSIFIER. */
	private static final String CLASSIFIER = COMMAND_ID + ".classifier";

	/**
	 * Instantiates a new adds the maven library to classpath handler.
	 */
	public AddMavenLibraryToClasspathHandler() {
	}

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		String group = event.getParameter(GROUP);
		String artifact = event.getParameter(ARTIFACT);
		String version = event.getParameter(VERSION);
		String classifier = event.getParameter(CLASSIFIER);
		List<IJavaProject> projects = JavaProjectHelper.getJavaProjects();
		if (projects.size() > 0) {
			ProjectPromptDialog userPromptDialog = new ProjectPromptDialog(HandlerUtil.getActiveShell(event), projects);
			userPromptDialog.setValidator(new IFieldsValidator() {

				@SuppressWarnings("rawtypes")
				public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
					StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
					String chosenProject = (String) fieldValueHolders.get(ProjectPromptDialog.CHOSEN_PROJECT_FIELD).getValue();
					if (chosenProject == null || chosenProject.trim().equals("")) {
						validationResult.append("Please choose a project.");
					}
					return validationResult;
				}

			});
			if (userPromptDialog.open() == Window.OK) {
				Job job = new AddMavenLibraryToClasspathJob(userPromptDialog.getChosenProject(), group, artifact, version, classifier);
				job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Job completion", "Javadoc and Sources magic ended: \n"));
				job.schedule();
			}
		} else {
			ErrorDialog errorDialog = new ErrorDialog("Error", "No available java projects");
			errorDialog.open();
		}
		return null;
	}
}
