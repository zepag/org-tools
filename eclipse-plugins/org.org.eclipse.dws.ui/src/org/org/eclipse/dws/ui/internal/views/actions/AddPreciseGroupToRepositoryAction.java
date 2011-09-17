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

import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.org.eclipse.core.utils.platform.actions.AbstractSimpleAction;
import org.org.eclipse.core.utils.platform.actions.IActionHost;
import org.org.eclipse.core.utils.platform.actions.IActionResolver;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldIdentifier;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldValueHolder;
import org.org.eclipse.core.utils.platform.dialogs.input.IFieldsValidator;
import org.org.eclipse.core.utils.platform.dialogs.input.IValidationResult;
import org.org.eclipse.core.utils.platform.dialogs.input.StringBuilderValidationResult;
import org.org.eclipse.dws.core.internal.dialogs.PreciseGroupPromptDialog;
import org.org.eclipse.dws.core.internal.jobs.AddPreciseGroupToRepositoryJob;
import org.org.eclipse.dws.core.internal.jobs.completion.CompletionPopupJobChangeListener;
import org.org.repository.crawler.maven2.model.CrawledRepository;


/**
 * The Class AddPreciseGroupToRepositoryAction.
 * 
 * @author pagregoire
 */
public class AddPreciseGroupToRepositoryAction extends AbstractSimpleAction {

	/** The resolver. */
	private IActionResolver resolver;

	/** The action host. */
	private IActionHost actionHost;

	/**
	 * Instantiates a new adds the precise group to repository action.
	 * 
	 * @param actionHost the action host
	 */
	public AddPreciseGroupToRepositoryAction(IActionHost actionHost) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) actionHost.getActionTrigger();
		Object next = selection.getFirstElement();
		if (next instanceof CrawledRepository) {
			final CrawledRepository crawledRepository = (CrawledRepository) next;
			PreciseGroupPromptDialog preciseGroupPromptDialog = new PreciseGroupPromptDialog(actionHost.getShell());
			preciseGroupPromptDialog.setValidator(new IFieldsValidator() {
				@SuppressWarnings("unchecked")
				public IValidationResult validate(Map<IFieldIdentifier, IFieldValueHolder> fieldValueHolders) {
					StringBuilderValidationResult validationResult = new StringBuilderValidationResult();
					IFieldValueHolder<String> fieldValueHolder = fieldValueHolders.get(PreciseGroupPromptDialog.GROUP_FIELD);
					String value = fieldValueHolder.getValue();
					if (value.trim().equals("")) {
						validationResult.append("Group Id cannot be empty");
					} else if (!value.matches("[A-Za-z0-9-_\\\\.\\\\*]*")) {
						validationResult.append("Group Id is invalid");
					}
					return validationResult;
				}

			});
			if (preciseGroupPromptDialog.open() == Window.OK) {
				final String dialogResult = preciseGroupPromptDialog.getPreciseGroup();
				Job job = new AddPreciseGroupToRepositoryJob(crawledRepository, dialogResult);
				job.addJobChangeListener(new CompletionPopupJobChangeListener("ORG DWS Notification","Group \"" + dialogResult + "\" 's addition to repository \"" + crawledRepository.getUID() + "\" scan ended: \n" ));
				job.schedule();
			}
		}

	}
}
