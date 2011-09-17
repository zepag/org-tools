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
package org.org.eclipse.dws.ui.internal.search;

import org.eclipse.osgi.util.NLS;


/**
 * The Class LibrarySearchMessages.
 */
public class LibrarySearchMessages extends NLS {
	static {
		NLS.initializeMessages(LibrarySearchMessages.class.getName(), LibrarySearchMessages.class);
	}
	
	/** The Search query_search for_name. */
	public static String SearchQuery_searchFor_name="Library name search for ''{0}'' in scope {1}";

	/** The Search query_status. */
	public static String SearchQuery_status="Found {0} matches.";

	/** The Search scope_workspace. */
	public static String SearchScope_workspace="workspace scope";

	/** The Search page_expression hint. */
	public static String SearchPage_expressionHint="";

	/** The Search page_expression. */
	public static String SearchPage_expression="Search string";

}