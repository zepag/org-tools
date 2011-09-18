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
package org.org.eclipse.core.utils.platform.dialogs.message;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.core.utils.platform.Messages;
import org.org.eclipse.core.utils.platform.PlatformUtilsPlugin;
import org.org.eclipse.core.utils.platform.images.PluginImages;


/**
 * Added a Details button to the AbstractInformationDialog to show the exception stack trace.
 */
public class WarningDialog extends AbstractInformationDialog {
	/**
	 * This constructor use the active Shell for the org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin.<br>
	 * If this plugin is not available in the context of the call (e.g. not started) use a constructor allowing the specification of the shell.<br>
	 * This constructor also uses a default image from this plugin.<br>
	 * If this plugin is not available in the context of the call (e.g. not started) use a constructor allowing the specification of the image.<br>
	 * 
	 * @param subTitle
	 *            The SubTitle displayed in the dialog
	 * @param dialogMessage
	 *            THe message to display
	 */
	public WarningDialog(String subTitle, String dialogMessage) {
		this(PlatformUtilsPlugin.getActiveShell(), PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.LOGO_BASIC_WARNING_64), subTitle, dialogMessage);
	}

	/**
	 * This constructor use the active Shell for the org.springframework.ide.eclipse.core.ui.PlatformUtilsPlugin If this plugin is not available in the context of the call (e.g. not started) use a constructor allowing the specification of the shell. This constructor also uses a default image from this plugin.<br>
	 * If this plugin is not available in the context of the call (e.g. not started) use a constructor allowing the specification of the image.<br>
	 * 
	 * Note that the details are formatted using the toString() method of the parameterized Object, but if the Object is a Throwable, the message and stacktrace are rendered.<br>
	 * 
	 * @param subTitle
	 *            The SubTitle displayed in the dialog
	 * @param dialogMessage
	 *            THe message to display
	 * @param detail
	 *            The expandable details
	 */
	public WarningDialog(String subTitle, String dialogMessage, Object detail) {
		this(PlatformUtilsPlugin.getActiveShell(), PlatformUtilsPlugin.getDefault().getImages().getImage(PluginImages.LOGO_BASIC_WARNING_64), subTitle, dialogMessage, detail);
	}

	/**
	 * This constructor allows you to use your own shell and image.<br>
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param image
	 *            the image to display
	 * @param subTitle
	 *            The SubTitle displayed in the dialog
	 * @param dialogMessage
	 *            THe message to display
	 */
	public WarningDialog(Shell parentShell, Image image, String subTitle, String dialogMessage) {
		this(PlatformUtilsPlugin.getActiveShell(), Messages.WarningDialog_title, subTitle, null, dialogMessage, null, new String[] { IDialogConstants.OK_LABEL}, 0, -1, image);
	}

	/**
	 * This constructor allows you to use your own shell and image.<br>
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param image
	 *            the image to display
	 * @param subTitle
	 *            The SubTitle displayed in the dialog
	 * @param dialogMessage
	 *            THe message to display
	 * @param detail
	 *            The expandable details
	 */
	public WarningDialog(Shell parentShell, Image image, String subTitle, String dialogMessage, Object detail) {
		this(PlatformUtilsPlugin.getActiveShell(), Messages.WarningDialog_title, subTitle, null, dialogMessage, detail, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.SHOW_DETAILS_LABEL }, 0, 1, image);
	}

	/**
	 * @param parentShell
	 * @param dialogTitle
	 * @param dialogSubTitle
	 * @param dialogTitleImage
	 * @param dialogMessage
	 * @param detail
	 * @param dialogButtonLabels
	 * @param defaultIndex
	 * @param detailIndex
	 * @param image
	 */
	public WarningDialog(Shell parentShell, String dialogTitle, String dialogSubTitle, Image dialogTitleImage, String dialogMessage, Object detail, String[] dialogButtonLabels, int defaultIndex, int detailIndex, Image image) {
		super(parentShell, dialogTitle, dialogSubTitle, dialogTitleImage, dialogMessage, dialogButtonLabels, defaultIndex, detailIndex);
		setDetail(detail);
		setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
		setImage(image);
	}

}