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
package org.org.eclipse.helpers.core.internal.xml;

import java.io.File;
import java.io.IOException;

public interface IProjectFileModifier {
	public static final String XML_VERSION_1_0 = "1.0";
	public static final String UTF_8 = "UTF-8";
	public static final String PROJECTDESCRIPTION = "projectDescription";
	public static final String BUILDSPEC = "buildSpec";
	public static final String BUILDCOMMAND = "buildCommand";
	public static final String NAME = "name";
	public static final String ARGUMENTS = "arguments";
	public static final String NATURES = "natures";
	public static final String NATURE = "nature";

	public void modifyProjectFile(File file) throws IOException;
}
