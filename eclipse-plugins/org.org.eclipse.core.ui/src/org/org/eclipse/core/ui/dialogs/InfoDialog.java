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
package org.org.eclipse.core.ui.dialogs;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.org.eclipse.core.ui.BasePlugin;
import org.org.eclipse.core.ui.images.PluginImages;


public class InfoDialog extends org.org.eclipse.core.utils.platform.dialogs.message.InfoDialog {

	public InfoDialog(Shell parentShell, Image image, String subTitle, String dialogMessage, Object detail) {
		super(parentShell, image, subTitle, dialogMessage, detail);
		setImage(BasePlugin.getDefault().getImages().getImage(PluginImages.LOGO_ORG_INFO_64));
	}

	public InfoDialog(Shell parentShell, Image image, String subTitle, String dialogMessage) {
		super(parentShell, image, subTitle, dialogMessage);
		setImage(BasePlugin.getDefault().getImages().getImage(PluginImages.LOGO_ORG_INFO_64));
	}

	public InfoDialog(Shell parentShell, String dialogTitle, String dialogSubTitle, Image dialogTitleImage, String dialogMessage, Object detail, String[] dialogButtonLabels, int defaultIndex, int detailIndex, Image image) {
		super(parentShell, dialogTitle, dialogSubTitle, dialogTitleImage, dialogMessage, detail, dialogButtonLabels, defaultIndex, detailIndex, image);
		setImage(BasePlugin.getDefault().getImages().getImage(PluginImages.LOGO_ORG_INFO_64));
	}

	public InfoDialog(String subTitle, String dialogMessage, Object detail) {
		super(subTitle, dialogMessage, detail);
		setImage(BasePlugin.getDefault().getImages().getImage(PluginImages.LOGO_ORG_INFO_64));
	}

	public InfoDialog(String subTitle, String dialogMessage) {
		super(subTitle, dialogMessage);
		setImage(BasePlugin.getDefault().getImages().getImage(PluginImages.LOGO_ORG_INFO_64));
	}

}
