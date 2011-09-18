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
package org.org.eclipse.dws.ui.internal.handlers;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.ui.dialogs.InfoDialog;
import org.org.eclipse.core.ui.dialogs.WarningDialog;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectHelper;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.IExecutionSavedContext;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.ui.internal.actions.ActionsMessages;
import org.org.eclipse.dws.ui.internal.wizards.PomUpdateWizard;
import org.org.eclipse.dws.ui.internal.wizards.WizardInitException;
import org.org.model.RootModelItem;
import org.org.repository.crawler.maven2.model.CrawledRepository;

/**
 * This Action launches the Pom update application Synchronization Wizard.
 * 
 * @author pagregoire
 */
public class PomUpdateHandler extends AbstractExtendedHandler<Object> {

	/** The Constant HANDLER_LABEL. */
	private final static String HANDLER_LABEL = "Parse pom action";
	// private final static String COMMAND_ID = "org.org.eclipse.dws.core.pomUpdateCommand";
	// private final static String POM_FILE = COMMAND_ID + ".pomFile";
	/** The logger. */
	private static Logger logger = Logger.getLogger(PomJavaSynchronizationHandler.class);

	public Object doExecute(ExecutionEvent event, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException {
		// FIXME PARAMETER/JOB
		IStructuredSelection structuredSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		try {
			if (!(RootModelItem.<CrawledRepository> getInstance(ModelConstants.REPOSITORIES_ROOT).hasChildren())) {
				WarningDialog warningDialog = new WarningDialog(HANDLER_LABEL, ActionsMessages.PomJavaSynchronizationActionDelegate_noRepositoryDefined);
				warningDialog.open();
			} else {
				IProject project = ((IFile) structuredSelection.getFirstElement()).getProject();
				if (JavaProjectHelper.isJavaProject(project)) {
					// FIXME use a Job
					IFile selectedFile = (IFile) structuredSelection.getFirstElement();
					PomUpdateWizard wizard = new PomUpdateWizard(selectedFile);
					WizardDialog dialog = new WizardDialog(PluginToolBox.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
					dialog.open();
				} else {
					WarningDialog warningDialog = new WarningDialog(HANDLER_LABEL, "Project is not a java project so synchronizing with the pom is meaningless");
					warningDialog.open();
				}
			}
		} catch (WizardInitException e) {
			manageWizardInitException(e);
		} catch (Throwable e) {
			logger.error(ActionsMessages.AbstractParsePomActionDelegate_unattendedException, e);
			ErrorDialog errorDialog = new ErrorDialog(HANDLER_LABEL, MessageFormat.format(ActionsMessages.AbstractParsePomActionDelegate_unattendedExceptionWithDescription, new Object[] { e.getClass().getName(), e.getMessage(), e }));
			errorDialog.open();
		}
		return null;
	}

	/**
	 * Manage wizard init exception.
	 * 
	 * @param e
	 *            the e
	 */
	public static void manageWizardInitException(WizardInitException e) {
		switch (e.getStatus()) {
		case ERROR:
			ErrorDialog errorDialog = new ErrorDialog(WizardInitException.Status.ERROR.name(), e.getMessage());
			errorDialog.open();
			break;
		case INFO:
			InfoDialog infoDialog = new InfoDialog(WizardInitException.Status.INFO.name(), e.getMessage());
			infoDialog.open();
			break;
		case WARNING:
			WarningDialog warningDialog = new WarningDialog(WizardInitException.Status.WARNING.name(), e.getMessage());
			warningDialog.open();
			break;
		default:
			break;
		}
	}

}