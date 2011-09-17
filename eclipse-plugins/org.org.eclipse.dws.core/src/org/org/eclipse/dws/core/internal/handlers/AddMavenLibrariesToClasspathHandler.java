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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectHelper;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;
import org.org.eclipse.core.utils.platform.tools.IOToolBox;
import org.org.eclipse.dws.core.internal.PomInteractionHelper;
import org.org.eclipse.dws.core.internal.dialogs.ProjectPromptDialog;
import org.org.eclipse.dws.core.internal.jobs.AddMavenLibrariesToClasspathJob;
import org.org.eclipse.dws.core.internal.jobs.LibraryDownloadTargetType;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomDependency;

public class AddMavenLibrariesToClasspathHandler extends AbstractExtendedHandler<Object> {

	/** The Constant COMMAND_ID. */
	private static final String COMMAND_ID = "org.org.eclipse.dws.core.addMavenLibrariesToClasspathCommand";

	/** The Constant POM_URL. */
	private static final String POM_URL = COMMAND_ID + ".pomUrl";

	/** The Constant TARGET_TYPE. */
	private static final String TARGET_TYPE = COMMAND_ID + ".targetType";

	/**
	 * Instantiates a new adds the maven libraries to classpath handler.
	 */
	public AddMavenLibrariesToClasspathHandler() {
		super();
	}

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {

		URL pomUrl = null;
		try {
			String pomUrlString = event.getParameter(POM_URL);
			pomUrl = new URL(pomUrlString);
		} catch (MalformedURLException e) {

			throw new ExtendedHandlerExecutionException("Impossible to parse file's URL", e);
		}
		/* target type determines the behaviour of the download */
		String targetTypeParameter = event.getParameter(TARGET_TYPE);
		LibraryDownloadTargetType targetType = LibraryDownloadTargetType.CLASSPATH;
		if (targetTypeParameter != null) {
			if (targetTypeParameter.equals(TargetTypeValues.PROJECT_CLASSPATH)) {
				targetType = LibraryDownloadTargetType.CLASSPATH;
			} else if (targetTypeParameter.equals(TargetTypeValues.WEB_INF_LIB)) {
				targetType = LibraryDownloadTargetType.WEBINFLIB;
			} else if (targetTypeParameter.equals(TargetTypeValues.TARGET_DIR)) {
				targetType = LibraryDownloadTargetType.FOLDER;
			}
		}
		java.util.List<IJavaProject> projects = JavaProjectHelper.getJavaProjects();
		if (projects.size() > 0) {
			ProjectPromptDialog userPromptDialog = new ProjectPromptDialog(HandlerUtil.getActiveShell(event), projects);
			userPromptDialog.setValidator(new IFieldsValidator() {

				@SuppressWarnings("unchecked")
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
				Proxy proxy = IOToolBox.determineProxy(pomUrl);
				Set<PomDependency> pomDependencies = null;
				try {
					InputStream pomStream = pomUrl.openConnection(proxy).getInputStream();
					Pom pom = PomInteractionHelper.parsePom(pomStream);
					pomDependencies = pom.getChildren();
				} catch (IOException e) {
					throw new ExtendedHandlerExecutionException("Impossible to open file: " + e.getClass().getName() + ":" + e.getMessage(), e);
				}
				if (pomDependencies != null) {
					Job job = new AddMavenLibrariesToClasspathJob(targetType, userPromptDialog.getChosenProject(), pomDependencies);
					job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Job completion", "Javadoc and Sources magic ended: \n"));
					job.schedule();
				}
			}
		} else {
			ErrorDialog errorDialog = new ErrorDialog("Error", "No available java projects");
			errorDialog.open();
		}
		return null;
	}
}