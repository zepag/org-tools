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
package org.org.eclipse.core.utils.jdt.tools;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.org.eclipse.core.utils.jdt.tools.ClasspathEntryDefinition.EntryType;

/**
 * @author pagregoire
 */
public final class ClasspathHelper {

	public static void addToClasspath(List<IClasspathEntry> classpath, IClasspathEntry library, IProgressMonitor monitor) {
		monitor.subTask("Adding " + library.getPath().toOSString() + " to the classpath.");
		classpath.add(library);
		monitor.worked(1);
	}

	/**
	 * This method allows to create a variable pointing to the given path.
	 * 
	 * @param variableName
	 * @param path
	 * @param monitor
	 */
	public static void createClasspathVariable(String variableName, IPath path, IProgressMonitor monitor) {
		try {
			JavaCore.setClasspathVariable(variableName, path, monitor);
		} catch (JavaModelException e) {
			throw new JavaProjectClasspathHelper.JavaProjectInteractionException("Impossible to create classpath variable " + variableName, e);
		}
	}

	/**
	 * This method allows to create a library entry in the project's classpath.
	 * 
	 * @param classpathEntryWrapper
	 * @param monitor
	 * @return
	 */
	public static IClasspathEntry createEntry(ClasspathEntryDefinition classpathEntryWrapper, IProgressMonitor monitor) {
		IClasspathEntry newLibraryEntry = null;
		IPath libraryPath = classpathEntryWrapper.getEntryPath().makeAbsolute();
		newLibraryEntry = JavaCore.newLibraryEntry(libraryPath, null, null);
		return newLibraryEntry;
	}

	/**
	 * This method creates a classpath variable from a classpath entry definition.
	 * 
	 * @param classpathEntryDefinition
	 * @param monitor
	 * @return
	 */
	public static IClasspathEntry createVariableEntry(ClasspathEntryDefinition classpathEntryDefinition, IProgressMonitor monitor) {
		IClasspathEntry newLibraryEntry = null;
		IPath sourcesAttachmentRootPath = null;
		IClasspathAttribute javadocClasspathAttribute = createJavadocClasspathAttribute(classpathEntryDefinition.getJavadocPath(), monitor);
		// Create the variable entry
		IClasspathAttribute[] classpathAttributes = (javadocClasspathAttribute == null ? new IClasspathAttribute[] {} : new IClasspathAttribute[] { javadocClasspathAttribute });
		IAccessRule[] accessRules = {};
		Boolean isExported = false;
		newLibraryEntry = JavaCore.newVariableEntry(classpathEntryDefinition.getEntryPath(), classpathEntryDefinition.getSourcesPath(), sourcesAttachmentRootPath, accessRules, classpathAttributes, isExported);
		return newLibraryEntry;
	}

	/**
	 * 
	 * @param classpathEntryWrapper
	 * @param targetName
	 * @param monitor
	 * @return
	 */
	public static IClasspathEntry createVariableEntryWithDifferentVariableName(ClasspathEntryDefinition classpathEntryWrapper, String targetName, IProgressMonitor monitor) {
		ClasspathEntryDefinition classpathEntryDefinition = replaceVariableWithOtherVariable(classpathEntryWrapper, targetName, monitor);
		return createVariableEntry(classpathEntryDefinition, monitor);
	}

	/**
	 * Returns the given variable's Path or null if unable to bind.
	 * 
	 * @param variableName
	 * @return
	 */
	public static IPath getClasspathVariablePath(String variableName) {
		return JavaCore.getClasspathVariable(variableName);
	}

	/**
	 * This method removes files from a project.
	 * 
	 * @param project
	 * @param entriesToRemove
	 * @param monitor
	 */
	public static void removeFilesFromProject(IProject project, List<IPath> entriesToRemove, IProgressMonitor monitor) {
		for (IPath path : entriesToRemove) {
			monitor.subTask("Removing " + path.toOSString() + " from the project.");
			try {
				IFile file = project.getParent().getFile(path);
				if (file.exists() && file.isSynchronized(IFile.DEPTH_INFINITE) && file.isAccessible() && !file.isLinked()) {
					file.delete(true, true, new NullProgressMonitor());
				}
			} catch (Exception e) {
				throw new JavaProjectClasspathHelper.JavaProjectInteractionException("Impossible to remove " + path.toOSString() + " from the classpath of project \"" + project.getName() + "\" : " + e.getMessage(), e);
			}
			monitor.worked(1);
		}
	}

	public static void removeFromClasspath(IJavaProject project, List<IClasspathEntry> classpath, List<IPath> entriesToRemove, IClasspathEntry library, IProgressMonitor monitor) {

		// if entry is from the project, delete it also...
		if (project.getProject().findMember(library.getPath().makeRelative()) != null) {
			entriesToRemove.add(library.getPath());
		}
		monitor.subTask("Removing " + library.getPath().toOSString() + " from the classpath.");
		classpath.remove(library);
		monitor.worked(1);
	}

	/**
	 * This method replaces a path segment in a path with a variable name.
	 * 
	 * @param originalPath
	 * @param pathToReplace
	 * @param variableName
	 * @param monitor
	 * 
	 * @return
	 */
	public static IPath replacePathWithVariable(IPath originalPath, IPath pathToReplace, String variableName, IProgressMonitor monitor) {
		IPath result = null;
		if (originalPath != null) {
			StringBuilder variablePathString = new StringBuilder(variableName);
			if (pathToReplace.isPrefixOf(originalPath)) {
				for (int i = 0; i < originalPath.segmentCount(); i++) {
					String s1 = pathToReplace.segment(i) == null ? "" : pathToReplace.segment(i);
					String s2 = originalPath.segment(i);
					if (s1 != null && !s1.equals(s2)) {
						variablePathString.append("/" + originalPath.segment(i));
					}
				}
				result = new Path(variablePathString.toString());
			} else {
				result = originalPath;
			}
		}
		return result;
	}

	public static ClasspathEntryDefinition replaceVariableWithOtherVariable(ClasspathEntryDefinition classpathEntryWrapper, String targetName, IProgressMonitor monitor) {
		// Init path from entry wrapper
		IPath entryPath = classpathEntryWrapper.getEntryPath();
		IPath entrySourcesPath = classpathEntryWrapper.getSourcesPath() == null ? null : classpathEntryWrapper.getSourcesPath();
		IPath entryJavadocPath = classpathEntryWrapper.getJavadocPath() == null ? null : classpathEntryWrapper.getJavadocPath();
		// Create appropriate path for each
		IPath variablePath = ClasspathHelper.replaceVariableWithTargetVariable(entryPath, targetName, monitor);
		IPath sourceAttachmentPath = ClasspathHelper.replaceVariableWithTargetVariable(entrySourcesPath, targetName, monitor);
		ClasspathEntryDefinition result = new ClasspathEntryDefinition(variablePath, sourceAttachmentPath, entryJavadocPath, EntryType.PATH);
		return result;
	}

	public static IPath replaceVariableWithTargetVariable(IPath path, String targetName, IProgressMonitor monitor) {
		IPath result = null;
		if (path != null) {
			StringBuilder variablePathString = new StringBuilder(targetName);
			for (int i = 1; i < path.segmentCount(); i++) {
				variablePathString.append("/" + path.segment(i));
			}
			result = new Path(variablePathString.toString());

		}
		return result;
	}

	private static IClasspathAttribute createJavadocClasspathAttribute(IPath javadocPath, IProgressMonitor monitor) {
		IClasspathAttribute result = null;
		if (javadocPath != null) {
			try {
				String value = javadocAttributeFromPath(javadocPath.toFile().toURI().toURL().toExternalForm());
				result = JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, value);
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		return result;
	}

	private static String javadocAttributeFromPath(String javadocPath) {
		String value = "jar:" + javadocPath + "!/";
		return value;
	}

	private ClasspathHelper() {
	}
}