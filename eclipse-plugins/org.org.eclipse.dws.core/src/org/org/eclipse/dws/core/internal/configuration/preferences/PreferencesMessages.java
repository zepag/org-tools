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

import org.eclipse.osgi.util.NLS;

/**
 * The Class PreferencesMessages.
 */
public class PreferencesMessages extends NLS {

	static {
		NLS.initializeMessages(PreferencesMessages.class.getName(), PreferencesMessages.class);
	}

	/** The General preference page_artifact download path. */
	public static String GeneralPreferencePage_artifactDownloadPath;

	/** The Advanced preference page_automatically add transitive. */
	public static String AdvancedPreferencePage_automaticallyAddTransitive;

	/** The Advanced preference page_automatically add transitive description. */
	public static String AdvancedPreferencePage_automaticallyAddTransitiveDescription;

	/** The Advanced preference page_automatically add undetermined. */
	public static String AdvancedPreferencePage_automaticallyAddUndetermined;

	/** The Advanced preference page_automatically add undetermined description. */
	public static String AdvancedPreferencePage_automaticallyAddUndeterminedDescription;

	/** The Advanced preference page_automatically remove conflicting. */
	public static String AdvancedPreferencePage_automaticallyRemoveConflicting;

	/** The Advanced preference page_automatically remove conflicting description. */
	public static String AdvancedPreferencePage_automaticallyRemoveConflictingDescription;

	/** The General preference page_comma separated pom names. */
	public static String GeneralPreferencePage_commaSeparatedPomNames;

	/** The Advanced preference page_consider optional. */
	public static String AdvancedPreferencePage_considerOptional;

	/** The Advanced preference page_consider optional description. */
	public static String AdvancedPreferencePage_considerOptionalDescription;

	/** The Advanced preference page_deal with transitive. */
	public static String AdvancedPreferencePage_dealWithTransitive;

	/** The Advanced preference page_deal with transitive description. */
	public static String AdvancedPreferencePage_dealWithTransitiveDescription;

	/** The Advanced preference page_deal with undetermined. */
	public static String AdvancedPreferencePage_dealWithUndetermined;

	/** The Advanced preference page_deal with undetermined description. */
	public static String AdvancedPreferencePage_dealWithUndeterminedDescription;

	/** The General preference page_default project folder. */
	public static String GeneralPreferencePage_defaultProjectFolder;

	/** The General preference page_default webinflib. */
	public static String GeneralPreferencePage_defaultWEBINFLIB;

	/** The General preference page_description. */
	public static String GeneralPreferencePage_description;

	/** The General preference page_variable name. */
	public static String GeneralPreferencePage_variableName;

	/** The Wizard preference page_description. */
	public static String WizardPreferencePage_description;

	/** The Wizard preference page_filtered libraries. */
	public static String WizardPreferencePage_filteredLibraries;

	/** The Wizard preference page_filtered jars. */
	public static String WizardPreferencePage_filteredJars;

	/** The DWS preference page_description. */
	public static String DWSPreferencePage_description;

	/** The Wizard preference page_filtered libraries popup. */
	public static String WizardPreferencePage_filteredLibrariesPopup;

	/** The Wizard preference page_filtered jars popup. */
	public static String WizardPreferencePage_filteredJarsPopup;

	/** The Wizard preference page_only keep exact match. */
	public static String WizardPreferencePage_onlyKeepExactMatch;

	/** The Wizard preference page_number of kept matches. */
	public static String WizardPreferencePage_numberOfKeptMatches;

	/** The Advanced preference page_description. */
	public static String AdvancedPreferencePage_description;
}
