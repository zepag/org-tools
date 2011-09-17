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
package org.org.eclipse.core.utils.platform;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.org.eclipse.core.utils.platform.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String InfoDialog_title;

	public static String AbstractMessageAndButtonDialog_font;

	public static String AbstractInformationDialog_error_detailsretrieval;

	public static String WarningDialog_title;

	public static String ErrorDialog_title;

	public static String ProcessTableItems_error_extensionpointresolution;

	public static String ProcessTableItems_error_extensionloading;

	public static String AbstractWizard_problem;

	public static String AbstractWorkbenchWizard_problem;
}
