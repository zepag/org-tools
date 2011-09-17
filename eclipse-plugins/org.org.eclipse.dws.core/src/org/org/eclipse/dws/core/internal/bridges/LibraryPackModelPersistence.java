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
package org.org.eclipse.dws.core.internal.bridges;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.org.eclipse.core.ui.dialogs.ErrorDialog;
import org.org.eclipse.core.utils.platform.binding.BindingException;
import org.org.eclipse.core.utils.platform.preferences.PreferencesFacade;
import org.org.eclipse.core.utils.platform.tools.PluginToolBox;
import org.org.eclipse.dws.core.DWSCorePlugin;
import org.org.eclipse.dws.core.internal.configuration.ConfigurationConstants;
import org.org.eclipse.dws.core.internal.configuration.preferences.PreferencesNames;
import org.org.eclipse.dws.core.internal.model.ModelConstants;
import org.org.eclipse.dws.core.internal.model.librarypack.LibraryPack;
import org.org.eclipse.dws.core.internal.xml.IXmlLibraryPacksBinder;
import org.org.eclipse.dws.core.internal.xml.StaxLibraryPacksBinder;
import org.org.model.RootModelItem;

/**
 * The Class LibraryPackModelPersistence.
 * 
 * @author pagregoire
 */
public class LibraryPackModelPersistence {
	/** The logger. */
	private static Logger logger = Logger.getLogger(LibraryPackModelPersistence.class);

	/** The Constant OUT_OF_SYNC. */
	public final static Integer OUT_OF_SYNC = Integer.valueOf(0);

	/** The Constant SYNC. */
	public final static Integer SYNC = Integer.valueOf(1);

	/** The workspace persistence status. */
	private static Integer workspacePersistenceStatus = OUT_OF_SYNC;

	/**
	 * Adds the library packs name autocomplete proposal.
	 * 
	 * @param proposal
	 *            the proposal
	 */
	public static void addLibraryPackNameAutocompleteProposal(String proposal) {
		String proposals = (String) PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_LIBRARYPACK_NAME_AUTOCOMPLETE, String.class);
		String frontSeparator = proposals.trim().endsWith(ConfigurationConstants.PIPE_SEPARATOR) ? "" : ConfigurationConstants.PIPE_SEPARATOR;
		proposals += frontSeparator + proposal + ConfigurationConstants.PIPE_SEPARATOR;
		PreferencesFacade.setPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_LIBRARYPACK_NAME_AUTOCOMPLETE, proposals);
	}

	/**
	 * Check status.
	 */
	public static void checkStatus() {
		if (getWorkspacePersistencesStatus().equals(OUT_OF_SYNC)) {
			saveLibraryPackInfo();
		}
	}

	/**
	 * Gets the library packs name autocomplete proposals.
	 * 
	 * @return the library packs name autocomplete proposals
	 */
	public static Set<String> getLibraryPackNameAutocompleteProposals() {
		Set<String> result = new TreeSet<String>();
		String proposals = (String) PreferencesFacade.getPreference(DWSCorePlugin.getDefault(), PreferencesNames.P_MAVEN_LIBRARYPACK_NAME_AUTOCOMPLETE, String.class);
		StringTokenizer tkz = new StringTokenizer(proposals, ConfigurationConstants.PIPE_SEPARATOR, false);
		while (tkz.hasMoreTokens()) {
			result.add(tkz.nextToken());
		}
		return result;
	}

	/**
	 * Load library packs info.
	 */
	public static void loadLibraryPackInfo() {
		logger.info("Loading Library Packs from file storage.");
		IPath persistedLibraryPacks = getPersistedLibraryPacksPath();
		InputStream contents = null;
		try {
			contents = new FileInputStream(persistedLibraryPacks.toFile());
			loadLibraryPackInfo(contents);
			logger.info("loaded library packs from file " + persistedLibraryPacks.toFile().toString() + ".");
		} catch (Exception e) {
			logger.error("unable to load library packs from file " + persistedLibraryPacks.toFile().toString() + ".");
		} finally {
			try {
				contents.close();
			} catch (Exception e) {
			}
		}
		setWorkspacePersistenceStatus(OUT_OF_SYNC);
	}

	private static IPath getPersistedLibraryPacksPath() {
		IPath persistedLibraryPacks = PluginToolBox.getCurrentWorkspace().getRoot().getRawLocation();
		persistedLibraryPacks = persistedLibraryPacks.append("/.org.org.eclipse.dws-librarypacks.xml");
		return persistedLibraryPacks;
	}

	/**
	 * Load library packs info.
	 * 
	 * @param contents
	 *            the contents
	 * @throws BindingException
	 * @param contents
	 *            the contents
	 */
	private static void loadLibraryPackInfo(InputStream contents) throws BindingException {
		IXmlLibraryPacksBinder xmlLibraryPacksBinder = new StaxLibraryPacksBinder();
		List<LibraryPack> libraryPacksModelItems = xmlLibraryPacksBinder.parseXmlLibraryPacks(contents);
		RootModelItem<LibraryPack> root = RootModelItem.<LibraryPack> getInstance(ModelConstants.LIBRARYPACKS_ROOT);
		root.toggleListenersOn();
		for (LibraryPack libraryPack : libraryPacksModelItems) {
			root.addChild(libraryPack);
		}
	}

	/**
	 * Save library packs info.
	 */
	public static void saveLibraryPackInfo() {
		IPath persistedLibraryPacks = getPersistedLibraryPacksPath();
		IXmlLibraryPacksBinder xmlLibraryPacksBinder = new StaxLibraryPacksBinder();
		try {
			xmlLibraryPacksBinder.toXmlLibraryPacks(new LinkedList<LibraryPack>(RootModelItem.<LibraryPack> getInstance(ModelConstants.LIBRARYPACKS_ROOT).getChildren()), new FileOutputStream(persistedLibraryPacks.toFile()));
			logger.info("saved library packs to file " + persistedLibraryPacks.toFile().toString() + ".");
			setWorkspacePersistenceStatus(SYNC);
		} catch (BindingException e) {
			logger.error("impossible to save library packs to file " + persistedLibraryPacks.toFile().toString() + ".", e);
			ErrorDialog errorDialog = new ErrorDialog("Binding Error", "An error occured while saving library packs information to file " + persistedLibraryPacks.toFile().toString() + ".", e);
			errorDialog.open();
		} catch (FileNotFoundException e) {
			logger.error("impossible to save library packs to file " + persistedLibraryPacks.toFile().toString() + ".", e);
			ErrorDialog errorDialog = new ErrorDialog("Binding Error", "An error occured while saving library packs information to file " + persistedLibraryPacks.toFile().toString() + ".", e);
			errorDialog.open();
		}

	}

	/**
	 * Sets the workspace persistence status.
	 * 
	 * @param status
	 *            the new workspace persistence status
	 */
	public static void setWorkspacePersistenceStatus(Integer status) {
		workspacePersistenceStatus = status;
	}

	/**
	 * Gets the workspace persistences status.
	 * 
	 * @return the workspace persistences status
	 */
	public static Integer getWorkspacePersistencesStatus() {
		return workspacePersistenceStatus;
	}

	/**
	 * Export library packs info.
	 * 
	 * @param libraryPacks
	 *            the library packs
	 * @param file
	 *            the file
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public static void exportLibraryPackInfo(Set<LibraryPack> libraryPacks, File file) throws FileNotFoundException {
		FileOutputStream baos = new FileOutputStream(file);
		IXmlLibraryPacksBinder xmlLibraryPacksBinder = new StaxLibraryPacksBinder();
		try {
			xmlLibraryPacksBinder.toXmlLibraryPacks(new LinkedList<LibraryPack>(libraryPacks), baos);
			logger.info("exported library packs model to file:" + file.toString());
			setWorkspacePersistenceStatus(SYNC);
		} catch (BindingException e) {
			ErrorDialog errorDialog = new ErrorDialog("Binding Error", "An error occured while exporting library packs information.", e);
			errorDialog.open();
		}
	}

	/**
	 * Import library packs info.
	 * 
	 * @param tmpFile
	 *            the tmp file
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	public static void importLibraryPackInfo(File tmpFile) throws FileNotFoundException {
		InputStream contents = new FileInputStream(tmpFile);
		if (tmpFile.exists() && !(tmpFile.length() == 0L)) {
			try {
				loadLibraryPackInfo(contents);
			} catch (Exception e) {
				logger.error("unable to load library packs from an external file.");
			} finally {
				try {
					contents.close();
				} catch (IOException e) {
				}
				logger.info("loaded library packs from an external file.");
				setWorkspacePersistenceStatus(OUT_OF_SYNC);
			}

		}

	}

	/**
	 * Import library packs info.
	 * 
	 * @param inputStream
	 *            the input stream
	 */
	public static void importLibraryPacksInfo(InputStream inputStream) {
		try {
			loadLibraryPackInfo(inputStream);
		} catch (Exception e) {
			logger.error("unable to load library packs from a stream.");
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
			logger.info("loaded library packs from a stream.");
			setWorkspacePersistenceStatus(OUT_OF_SYNC);
		}

	}

}
