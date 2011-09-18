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
package org.org.eclipse.dws.core.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.org.eclipse.dws.core.internal.model.ParentPom;
import org.org.eclipse.dws.core.internal.model.Pom;
import org.org.eclipse.dws.core.internal.model.PomRepository;

/**
 * The Class PomPropertiesHelper.
 */
public class PomPropertiesHelper {

	/** The Constant PROPERTY_PATTERN. */
	private static final String PROPERTY_PATTERN = "[.[^\\$\\{\\}]]*\\$\\{([a-zA-Z0-9\\.]+)\\}[.[^\\$\\{\\}]]*";

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		Map<String, String> props = new HashMap<String, String>();
		props.put("blabla.blabla", "c:\\java\\blabla\\");
		props.put("bloblo.bloblo", "valueofbloblo");
		props.put("bleble.bleble", "valueofbleble");
		Map<String, String> props2 = new HashMap<String, String>();
		props2.put("blabla.blabla", "blast");
		props2.put("bleble.bleble", "blast");
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		list.add(props2);
		list.add(props);

		final String test1 = "${blabla.blabla} ${bloblo.bloblo}";
		final String test2 = "<tag>${blabla.blabla} afterblabla ${bloblo.bloblo}</tag>";
		final String test3 = "\t\t   <namespace:tag>${blabla.blabla} afterblabla ${bloblo.bloblo}</namespace:tag>";
		final String test4 = "\t\t   <namespace:tag attribute=\"value\">${blabla.blabla} afterblabla ${bloblo.bloblo}</namespace:tag>";
		final String test5 = "\t\t   <namespace:tag attribute=\"value\">${blabla.blabla} and ${bloblo.bloblo} and ${bleble.bleble}</namespace:tag>";
		System.out.println(substitutePropertyWithIn("blabla.blabla", "blibli.blibli", test1));
		System.out.println(substitutePropertyWithIn("blabla.blabla", "blibli.blibli", test2));
		System.out.println(substitutePropertyWithIn("blabla.blabla", "blibli.blibli", test3));
		System.out.println(substitutePropertyWithIn("blabla.blabla", "blibli.blibli", test4));
		System.out.println();
		System.out.println(replacePropertiesIn(test1, props));
		System.out.println(replacePropertiesIn(test2, props));
		System.out.println(replacePropertiesIn(test3, props));
		System.out.println(replacePropertiesIn(test4, props));
		System.out.println(replacePropertiesIn(test5, list));
	}

	/**
	 * Replace properties in.
	 * 
	 * @param targetString
	 *            the target string
	 * @param props
	 *            the props
	 * 
	 * @return the string
	 */
	public static String replacePropertiesIn(String targetString, Map<String, String> props) {
		String result = targetString;
		if (result.contains("${")) {
			Set<String> keys = new HashSet<String>();
			Matcher matcher = Pattern.compile(PROPERTY_PATTERN).matcher(result);
			while (matcher.find()) {
				String propertyName = matcher.group(1);
				keys.add(propertyName);
			}
			for (String key : keys) {
				result = substitutePropertyWithIn(key, props.get(key), result);
			}
		}
		return result;
	}

	/**
	 * Replace properties in.
	 * 
	 * @param targetString
	 *            the target string
	 * @param orderedPropertiesSets
	 *            the ordered properties sets
	 * 
	 * @return the string
	 */
	public static String replacePropertiesIn(String targetString, List<Map<String, String>> orderedPropertiesSets) {
		String result = targetString;
		if (result.contains("${")) {
			Set<String> keys = new HashSet<String>();
			Matcher matcher = Pattern.compile(PROPERTY_PATTERN).matcher(result);
			while (matcher.find()) {
				String propertyName = matcher.group(1);
				keys.add(propertyName);
			}
			for (String key : keys) {
				for (Map<String, String> props : orderedPropertiesSets) {
					String value = props.get(key);
					if (value != null) {
						result = substitutePropertyWithIn(key, value, result);
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Substitute property with in.
	 * 
	 * @param propertyName
	 *            the property name
	 * @param replacementString
	 *            the replacement string
	 * @param evaluatedString
	 *            the evaluated string
	 * 
	 * @return the string
	 */
	public static String substitutePropertyWithIn(String propertyName, String replacementString, String evaluatedString) {
		replacementString = replacementString.replace("\\", "\\\\");
		return evaluatedString.replaceAll("\\$\\{" + propertyName + "\\}", replacementString);
	}

	/**
	 * Do properties resolution.
	 * 
	 * @param pomParsingDescription
	 *            the pom parsing description
	 * @param pomPropertiesSets
	 *            the pom properties sets
	 * 
	 * @return the parsed pom description
	 */
	public static Pom doPropertiesResolution(Pom pomParsingDescription, List<Map<String, String>> pomPropertiesSets) {
		pomParsingDescription.setArtifactId(replacePropertiesIn(pomParsingDescription.getArtifactId(), pomPropertiesSets));
		pomParsingDescription.setGroupId(replacePropertiesIn(pomParsingDescription.getGroupId(), pomPropertiesSets));
		pomParsingDescription.setPackaging(replacePropertiesIn(pomParsingDescription.getPackaging(), pomPropertiesSets));
		pomParsingDescription.setVersion(replacePropertiesIn(pomParsingDescription.getVersion(), pomPropertiesSets));
		doPropertiesResolution(pomParsingDescription.getParentPom(), pomPropertiesSets);
		for (PomRepository pomRepository : pomParsingDescription.getRepositories().getPomRepositories().values()) {
			doPropertiesResolution(pomRepository, pomPropertiesSets);
		}
		return pomParsingDescription;
	}

	/**
	 * Do properties resolution.
	 * 
	 * @param pomRepository
	 *            the parsed repository description
	 * @param pomPropertiesSets
	 *            the pom properties sets
	 * 
	 * @return the parsed repository description
	 */
	public static PomRepository doPropertiesResolution(PomRepository pomRepository, List<Map<String, String>> pomPropertiesSets) {
		pomRepository.setId(replacePropertiesIn(pomRepository.getId(), pomPropertiesSets));
		pomRepository.setName(replacePropertiesIn(pomRepository.getName(), pomPropertiesSets));
		pomRepository.setUrl(replacePropertiesIn(pomRepository.getUrl(), pomPropertiesSets));
		return pomRepository;
	}

	/**
	 * Do properties resolution.
	 * 
	 * @param parentPom
	 *            the parent pom description
	 * @param pomPropertiesSets
	 *            the pom properties sets
	 * 
	 * @return the parent pom description
	 */
	public static ParentPom doPropertiesResolution(ParentPom parentPom, List<Map<String, String>> pomPropertiesSets) {
		parentPom.setArtifactId(replacePropertiesIn(parentPom.getArtifactId(), pomPropertiesSets));
		parentPom.setGroupId(replacePropertiesIn(parentPom.getGroupId(), pomPropertiesSets));
		parentPom.setRelativePath(replacePropertiesIn(parentPom.getRelativePath(), pomPropertiesSets));
		parentPom.setVersion(replacePropertiesIn(parentPom.getVersion(), pomPropertiesSets));
		return parentPom;
	}
}