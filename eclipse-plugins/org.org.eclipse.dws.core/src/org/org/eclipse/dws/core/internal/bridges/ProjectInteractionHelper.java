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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.org.eclipse.core.utils.jdt.tools.ClasspathHelper;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectClasspathHelper;
import org.org.eclipse.core.utils.jdt.tools.ClasspathEntryDefinition;
import org.org.eclipse.core.utils.jdt.tools.JavaProjectClasspathHelper.JavaProjectInteractionException;
import org.org.eclipse.core.utils.jdt.tools.ClasspathEntryDefinition.EntryType;
import org.org.eclipse.core.utils.platform.tools.FileToolBox;
import org.org.eclipse.core.utils.platform.tools.ProjectHelper;
import org.org.eclipse.dws.core.internal.configuration.AggregatedProperties;
import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;


/**
 * The Class ProjectInteractionHelper.
 */
public final class ProjectInteractionHelper {

	/**
	 * Instantiates a new project interaction helper.
	 */
	private ProjectInteractionHelper() {
	}

	/**
	 * The Class ProjectInteractionException.
	 */
	public static class ProjectInteractionException extends RuntimeException {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1505211004084925793L;

		/**
		 * Instantiates a new project interaction exception.
		 */
		public ProjectInteractionException() {
			super();
		}

		/**
		 * Instantiates a new project interaction exception.
		 * 
		 * @param message the message
		 * @param cause the cause
		 */
		public ProjectInteractionException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Instantiates a new project interaction exception.
		 * 
		 * @param message the message
		 */
		public ProjectInteractionException(String message) {
			super(message);
		}

		/**
		 * Instantiates a new project interaction exception.
		 * 
		 * @param cause the cause
		 */
		public ProjectInteractionException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * Adds the to raw classpath.
	 * 
	 * @param classpathEntryWrappers the classpath entry wrappers
	 * @param javaProject the java project
	 * @param monitor the monitor
	 * 
	 * @return the list< classpath entry definition>
	 */
	private static List<ClasspathEntryDefinition> addToRawClasspath(List<ClasspathEntryDefinition> classpathEntryWrappers, IJavaProject javaProject, IProgressMonitor monitor) {
		monitor.beginTask("Adding libraries to the classpath of project :" + javaProject.getElementName(), classpathEntryWrappers.size() + 1);
		IClasspathEntry[] rawClassPath = JavaProjectClasspathHelper.getRawClasspath(javaProject);
		List<IClasspathEntry> formerEntries = new LinkedList<IClasspathEntry>(Arrays.asList(rawClassPath));
		List<IClasspathEntry> newEntries = new LinkedList<IClasspathEntry>();
		for (ClasspathEntryDefinition classpathEntryWrapper : classpathEntryWrappers) {
			IClasspathEntry newLibraryEntry = null;
			if (classpathEntryWrapper.getEntryType() == EntryType.PATH) {
				newLibraryEntry = ClasspathHelper.createEntry(classpathEntryWrapper, monitor);
			} else {
				newLibraryEntry = createDWSVariableEntry(classpathEntryWrapper, javaProject, monitor);
			}
			// ClasspathHelper.addToClasspath(classpath, newLibraryEntry, monitor);
			newEntries.add(newLibraryEntry);
			classpathEntryWrapper.setClasspathEntry(javaProject.encodeClasspathEntry(newLibraryEntry));
		}
		JavaProjectClasspathHelper.updateRawClasspath(javaProject, formerEntries, newEntries, monitor);
		return classpathEntryWrappers;
	}

	/*
	 * variablePath the path of the binary archive; first segment is the name of a classpath variable
	 * 
	 * variableSourceAttachmentPath the path of the corresponding source archive, or null if none; if present, the first segment is the name of a classpath variable (not necessarily the same variable as the one that begins variablePath) variableSourceAttachmentRootPath the location of the root of the source files within the source archive or null if variableSourceAttachmentPath is also null accessRules the possibly empty list of access rules for this entry extraAttributes the possibly empty list of extra attributes to persist with this entry isExported indicates whether this entry is contributed to dependent projects in addition to the output location
	 * 
	 */
	/**
	 * Creates the dws variable entry.
	 * 
	 * @param classpathEntryWrapper the classpath entry wrapper
	 * @param javaProject the java project
	 * @param monitor the monitor
	 * 
	 * @return the i classpath entry
	 */
	private static IClasspathEntry createDWSVariableEntry(ClasspathEntryDefinition classpathEntryWrapper, IJavaProject javaProject, IProgressMonitor monitor) {
		createDWSClasspathVariableIfNotExists(javaProject, monitor);
		// Retrieve variable name and repository path from preferences/project properties
		String variableName = AggregatedProperties.getVariableName(javaProject.getProject());
		IPath repositoryPath = new Path(AggregatedProperties.getLocalRepository(javaProject.getProject()));
		// substitute repository path with DWS variable
		ClasspathEntryDefinition classpathEntryDefinition = replaceRepositoryPathWithDWSVariable(classpathEntryWrapper, repositoryPath, variableName, monitor);
		return ClasspathHelper.createVariableEntry(classpathEntryDefinition, monitor);
	}

	/**
	 * Replace repository path with dws variable.
	 * 
	 * @param classpathEntryWrapper the classpath entry wrapper
	 * @param repositoryPath the repository path
	 * @param variableName the variable name
	 * @param monitor the monitor
	 * 
	 * @return the classpath entry definition
	 */
	private static ClasspathEntryDefinition replaceRepositoryPathWithDWSVariable(ClasspathEntryDefinition classpathEntryWrapper, IPath repositoryPath, String variableName, IProgressMonitor monitor) {
		// Init path from entry wrapper
		IPath entryPath = classpathEntryWrapper.getEntryPath();
		IPath entrySourcesPath = classpathEntryWrapper.getSourcesPath() == null ? null : classpathEntryWrapper.getSourcesPath();
		IPath entryJavadocPath = classpathEntryWrapper.getJavadocPath() == null ? null : classpathEntryWrapper.getJavadocPath();
		// Create appropriate path for each
		IPath variablePath = ClasspathHelper.replacePathWithVariable(entryPath, repositoryPath, variableName, monitor);
		IPath sourceAttachmentPath = ClasspathHelper.replacePathWithVariable(entrySourcesPath, repositoryPath, variableName, monitor);
		ClasspathEntryDefinition result = new ClasspathEntryDefinition(variablePath, sourceAttachmentPath, entryJavadocPath, EntryType.VARIABLE);
		return result;
	}

	/**
	 * Creates the dws classpath variable if not exists.
	 * 
	 * @param javaProject the java project
	 * @param monitor the monitor
	 * 
	 * @return the string
	 */
	public static String createDWSClasspathVariableIfNotExists(IJavaProject javaProject, IProgressMonitor monitor) {
		IProject project = javaProject != null ? javaProject.getProject() : null;
		String repositoryLocation = AggregatedProperties.getLocalRepository(project);
		String variableName = AggregatedProperties.getVariableName(project);
		if (!canBindDWSClasspathVariable(javaProject, monitor)) {
			ClasspathHelper.createClasspathVariable(variableName, new Path(repositoryLocation), monitor);
		} else {
			if (!isDWSRepositoryPathSameAsVariablePath(repositoryLocation, javaProject, monitor)) {
				ClasspathHelper.createClasspathVariable(variableName, new Path(AggregatedProperties.getLocalRepository(project)), monitor);
			}
		}
		return variableName;
	}

	/**
	 * Checks if is dWS repository path same as variable path.
	 * 
	 * @param repositoryLocation the repository location
	 * @param javaProject the java project
	 * @param monitor the monitor
	 * 
	 * @return true, if is dWS repository path same as variable path
	 */
	private static boolean isDWSRepositoryPathSameAsVariablePath(String repositoryLocation, IJavaProject javaProject, IProgressMonitor monitor) {
		IProject project = javaProject != null ? javaProject.getProject() : null;
		IPath classpathVariablePath = ClasspathHelper.getClasspathVariablePath(AggregatedProperties.getVariableName(project));
		return classpathVariablePath.makeAbsolute().toOSString().equals(repositoryLocation);
	}

	/**
	 * Can bind dws classpath variable.
	 * 
	 * @param javaProject the java project
	 * @param monitor the monitor
	 * 
	 * @return true, if successful
	 */
	public static boolean canBindDWSClasspathVariable(IJavaProject javaProject, IProgressMonitor monitor) {
		IProject project = javaProject != null ? javaProject.getProject() : null;
		IPath classpathVariablePath = ClasspathHelper.getClasspathVariablePath(AggregatedProperties.getVariableName(project));
		return classpathVariablePath != null;
	}

	/**
	 * Adds the to classpath.
	 * 
	 * @param entriesDefinitions the entries definitions
	 * @param javaProject the java project
	 * @param monitor the monitor
	 * 
	 * @return the list< classpath entry definition>
	 */
	public static List<ClasspathEntryDefinition> addToClasspath(List<ClasspathEntryDefinition> entriesDefinitions, IJavaProject javaProject, IProgressMonitor monitor) {
		return addToRawClasspath(entriesDefinitions, javaProject, monitor);
	}

	/**
	 * Creates the folder and add libraries.
	 * 
	 * @param folderPath the folder path
	 * @param librariesFiles the libraries files
	 * @param project the project
	 * @param monitor the monitor
	 */
	public static void createFolderAndAddLibraries(IFolder folderPath, List<File> librariesFiles, IProject project, IProgressMonitor monitor) {
		monitor.beginTask("Adding libraries to the project :" + project.getName(), librariesFiles.size() + 2);
		if (!folderPath.exists()) {
			try {
				FileToolBox.createFolders(folderPath, monitor);
			} catch (CoreException e) {
				throw new ProjectInteractionException("Impossible to create folder " + folderPath, e);
			}
		}
		try {
			FileToolBox.addToFolder(librariesFiles, folderPath, monitor);
		} catch (CoreException e) {
			throw new ProjectInteractionException("Impossible to add libraries " + librariesFiles + " to folder " + folderPath, e);
		} catch (IOException e) {
			throw new ProjectInteractionException("Impossible to add libraries " + librariesFiles + " to folder " + folderPath, e);
		}
		try {
			ProjectHelper.doRefresh(folderPath.getProject(), monitor);
		} catch (CoreException e) {
			throw new ProjectInteractionException("Impossible to refresh the state of project :" + folderPath.getProject().getName(), e);
		}
	}

	/**
	 * Gets the classpath entries.
	 * 
	 * @param javaProject the java project
	 * 
	 * @return the classpath entries
	 */
	public static Set<DWSClasspathEntryDescriptor> getClasspathEntries(IJavaProject javaProject) {
		Set<DWSClasspathEntryDescriptor> classpathEntryDescriptors = new LinkedHashSet<DWSClasspathEntryDescriptor>();
		try {
			if (javaProject.exists()) {
				for (IClasspathEntry classpathEntry : javaProject.getRawClasspath()) {
					DWSClasspathEntryDescriptor classpathEntryDescriptor = new DWSClasspathEntryDescriptor();
					classpathEntryDescriptor.setEncodedClasspathEntry(javaProject.encodeClasspathEntry(classpathEntry));
					classpathEntryDescriptor.setPath(classpathEntry.getPath().toPortableString());
					classpathEntryDescriptor.setProjectName(javaProject.getElementName());
					IJavaModelStatus javaModelStatus = JavaConventions.validateClasspathEntry(javaProject, classpathEntry, false);
					classpathEntryDescriptor.setValid(classpathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY || classpathEntry.getEntryKind() == IClasspathEntry.CPE_VARIABLE && javaModelStatus.isOK());
					classpathEntryDescriptors.add(classpathEntryDescriptor);
				}
			}
		} catch (JavaModelException e) {
			throw new JavaProjectInteractionException("An error occured while scanning for classpath entries in project:" + javaProject, e);
		}
		return classpathEntryDescriptors;
	}

	/**
	 * Removes the from classpath.
	 * 
	 * @param classpathEntries the classpath entries
	 * @param project the project
	 * @param monitor the monitor
	 */
	public static void removeFromClasspath(Collection<DWSClasspathEntryDescriptor> classpathEntries, IProject project, IProgressMonitor monitor) {
		removeFromRawClasspath(classpathEntries, project, monitor);
	}

	/**
	 * Removes the from raw classpath.
	 * 
	 * @param classpathEntries the classpath entries
	 * @param project the project
	 * @param monitor the monitor
	 */
	private static void removeFromRawClasspath(Collection<DWSClasspathEntryDescriptor> classpathEntries, IProject project, IProgressMonitor monitor) {
		monitor.beginTask("Removing libraries from the classpath of project :" + project.getName(), classpathEntries.size() + 1);
		IJavaProject javaProject = JavaCore.create(project);
		List<IClasspathEntry> formerEntries = JavaProjectClasspathHelper.getRawClasspathAsList(javaProject);
		List<IPath> entriesToRemove = new LinkedList<IPath>();
		List<IClasspathEntry> newEntries = new LinkedList<IClasspathEntry>();
		for (DWSClasspathEntryDescriptor classpathEntryDescriptor : classpathEntries) {
			ClasspathHelper.removeFromClasspath(javaProject, formerEntries, entriesToRemove, javaProject.decodeClasspathEntry(classpathEntryDescriptor.getEncodedClasspathEntry()), monitor);
		}
		JavaProjectClasspathHelper.updateRawClasspath(javaProject, formerEntries, newEntries, monitor);
		monitor.beginTask("Removing files from project :" + project.getName(), entriesToRemove.size());
		ClasspathHelper.removeFilesFromProject(project, entriesToRemove, monitor);
	}

}
