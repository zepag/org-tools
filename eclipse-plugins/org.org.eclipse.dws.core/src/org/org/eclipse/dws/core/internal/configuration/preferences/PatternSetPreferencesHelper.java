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
package org.org.eclipse.dws.core.internal.configuration.preferences;

import org.org.eclipse.dws.core.internal.configuration.ConfigurationConstants;
import org.org.repository.crawler.items.IPatternSet;
import org.org.repository.crawler.items.mutable.PatternSet;
import org.org.repository.crawler.maven2.model.protocolplugins.HttpRepositoryBrowserPlugin;


/**
 * The Class PatternSetPreferencesHelper.
 */
public class PatternSetPreferencesHelper {
	
	/**
	 * Deserialize.
	 * 
	 * @param string the string
	 * 
	 * @return the i pattern set
	 */
	public static IPatternSet deserialize(String string) {
		String[] parts = string.split(ConfigurationConstants.PIPE_SEPARATOR_REGEXP);
		PatternSet patternSet = new PatternSet();
		patternSet.setLabel(parts[0]);
		patternSet.setEntryPattern(parts[1]);
		patternSet.setFileEntryPattern(parts[2]);
		patternSet.setDirectoryEntryPattern(parts[3]);
		patternSet.setParentDirectoryPattern(parts[4]);
		return patternSet.getImmutable();
	}

	/**
	 * Serialize.
	 * 
	 * @param patternSet the pattern set
	 * 
	 * @return the string
	 */
	public static String serialize(IPatternSet patternSet) {
		StringBuilder builder = new StringBuilder();
		builder.append(patternSet.getLabel() + ConfigurationConstants.PIPE_SEPARATOR);
		builder.append(patternSet.getEntryPattern() + ConfigurationConstants.PIPE_SEPARATOR);
		builder.append(patternSet.getFileEntryPattern() + ConfigurationConstants.PIPE_SEPARATOR);
		builder.append(patternSet.getDirectoryEntryPattern() + ConfigurationConstants.PIPE_SEPARATOR);
		builder.append(patternSet.getParentDirectoryPattern() + ConfigurationConstants.PIPE_SEPARATOR);
		return builder.toString();
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		PatternSet patternSet = new PatternSet();
		patternSet.setLabel("blabla");
		patternSet.setEntryPattern("entry");
		patternSet.setFileEntryPattern("file");
		patternSet.setDirectoryEntryPattern("directory");
		patternSet.setParentDirectoryPattern("parent");
		System.out.println(serialize(patternSet));
		IPatternSet result = deserialize(serialize(patternSet));
		System.out.println(result);

		System.out.println(serialize(HttpRepositoryBrowserPlugin.TOMCAT6_PATTERNSET));
	}
}
