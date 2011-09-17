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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.org.eclipse.core.utils.jdt.tools.ClasspathEntryDefinition.EntryType;

public class JavaProjectClasspathHelper {

	public static class JavaProjectInteractionException extends RuntimeException {

		private static final long serialVersionUID = -3299504145509021907L;

		public JavaProjectInteractionException() {
			super();
		}

		public JavaProjectInteractionException(String message) {
			super(message);
		}

		public JavaProjectInteractionException(String message, Throwable cause) {
			super(message, cause);
		}

		public JavaProjectInteractionException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * This helper methods replaces a variable name with another in all matching entries of a project. If the target classpath variable doesn't exist, it is created on the fly.
	 * 
	 * @param originalName
	 * @param targetName
	 * @param javaProject
	 * @param monitor
	 */
	public static void changeClasspathVariable(String originalName, String targetName, IJavaProject javaProject, IProgressMonitor monitor) {
		try {
			if (javaProject == null) {
				throw new JavaProjectInteractionException("Project should not be null.");
			}
			if (JavaCore.getClasspathVariable(targetName) == null) {
				JavaCore.setClasspathVariable(targetName, JavaCore.getClasspathVariable(originalName), monitor);
			}

			IClasspathEntry[] classpathEntries = getRawClasspath(javaProject);
			List<IClasspathEntry> targetClasspathEntries = new ArrayList<IClasspathEntry>();
			for (IClasspathEntry classpathEntry : classpathEntries) {
				if (!JavaProjectClasspathHelper.isVariableEntry(classpathEntry)) {
					targetClasspathEntries.add(classpathEntry);
				} else {
					IPath sourceAttachmentPath = classpathEntry.getSourceAttachmentPath();
					IClasspathAttribute javadocClasspathAttribute = null;
					for (IClasspathAttribute classpathAttribute : classpathEntry.getExtraAttributes()) {
						if (classpathAttribute.getName().equals(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME)) {
							javadocClasspathAttribute = classpathAttribute;
						}
					}
					IPath javadocPath = null;
					if (javadocClasspathAttribute != null && javadocClasspathAttribute.getValue() != null) {
						javadocPath = JavaProjectClasspathHelper.pathFromJavadocAttributeValue(javadocClasspathAttribute);
					}
					ClasspathEntryDefinition classpathEntryWrapper = new ClasspathEntryDefinition(classpathEntry.getPath(), sourceAttachmentPath, javadocPath, EntryType.VARIABLE);
					targetClasspathEntries.add(ClasspathHelper.createVariableEntryWithDifferentVariableName(classpathEntryWrapper, targetName, monitor));
				}
			}
			JavaProjectClasspathHelper.updateRawClasspath(javaProject, targetClasspathEntries, new SubProgressMonitor(monitor, 1));
		} catch (Exception e) {
			throw new JavaProjectInteractionException(e);
		}
	}

	/**
	 * This method allows to retrieve the raw classpath of a given java project.
	 * 
	 * @param javaProject
	 * @return
	 * @throws JavaProjectInteractionException
	 */
	public static IClasspathEntry[] getRawClasspath(IJavaProject javaProject) throws JavaProjectInteractionException {
		IClasspathEntry[] rawClassPath = null;
		try {
			rawClassPath = javaProject.getRawClasspath();
		} catch (JavaModelException e) {
			throw new JavaProjectInteractionException("Impossible to get the raw classpath of project \"" + javaProject.getProject().getName() + "\" : " + e.getMessage(), e);
		}
		return rawClassPath;
	}

	public static List<IClasspathEntry> getRawClasspathAsList(IJavaProject javaProject) {
		List<IClasspathEntry> result = new LinkedList<IClasspathEntry>();
		IClasspathEntry[] rawClassPath = getRawClasspath(javaProject);
		for (IClasspathEntry classpathEntry : rawClassPath) {
			result.add(classpathEntry);
		}
		return result;
	}

	public static void updateRawClasspath(IJavaProject javaProject, List<IClasspathEntry> classpath, IProgressMonitor monitor) throws JavaModelException {
		monitor.subTask("Updating the classpath of project " + javaProject.getElementName() + ".");
		javaProject.setRawClasspath((IClasspathEntry[]) classpath.toArray(new IClasspathEntry[0]), new NullProgressMonitor());
		monitor.worked(1);
	}

	/**
	 * This method update a project's classpath with new entries.
	 * 
	 * @param javaProject
	 * @param formerEntries
	 * @param newEntries
	 * @param monitor
	 */
	public static void updateRawClasspath(IJavaProject javaProject, List<IClasspathEntry> formerEntries, List<IClasspathEntry> newEntries, IProgressMonitor monitor) {
		List<String> errorMessages = new LinkedList<String>();
		try {
			List<IClasspathEntry> allEntries = new LinkedList<IClasspathEntry>(formerEntries);
			allEntries.addAll(newEntries);
			JavaProjectClasspathHelper.updateRawClasspath(javaProject, allEntries, monitor);
		} catch (JavaModelException e) {
			// IF AN EXCEPTION OCCURS, LET's NAIL DOWN THE ENTRY CAUSING IT
			List<IClasspathEntry> addedEntries = new LinkedList<IClasspathEntry>(formerEntries);
			for (IClasspathEntry classpathEntry : newEntries) {
				addedEntries.add(classpathEntry);
				try {
					JavaProjectClasspathHelper.updateRawClasspath(javaProject, addedEntries, monitor);
				} catch (JavaModelException e2) {
					addedEntries.remove(classpathEntry);
					// IF ERROR IS A NAME COLLISION, THIS MEANS THAT ENTRY ALREADY EXISTS...BUT WAS NOT PROPERLY RESOLVED PRIOR TO THIS OPERATION
					if (!(e2.getJavaModelStatus().getCode() == IJavaModelStatusConstants.NAME_COLLISION)) {
						errorMessages.add("entry \"" + classpathEntry + "\" : " + e.getMessage());
					}
				}
			}
		}
		if (errorMessages.size() > 0) {
			final String CRLF = "\n";
			StringBuilder stringBuilder = new StringBuilder("Errors occured while updating classpath:" + CRLF);
			for (String errorMessage : errorMessages) {
				stringBuilder.append("\t" + errorMessage + CRLF);
			}
			throw new JavaProjectInteractionException(stringBuilder.toString());
		}
	}

	private static boolean isVariableEntry(IClasspathEntry classpathEntry) {
		return classpathEntry.getEntryKind() == IClasspathEntry.CPE_VARIABLE;
	}

	private static IPath pathFromJavadocAttributeValue(IClasspathAttribute javadocClasspathAttribute) {
		IPath path = null;
		if (javadocClasspathAttribute != null && javadocClasspathAttribute.getValue() != null) {
			Pattern pattern = Pattern.compile("jar:(.*)!/");
			Matcher matcher = pattern.matcher(javadocClasspathAttribute.getValue());
			matcher.find();
			path = Path.fromPortableString(matcher.group(1));
		}
		return path;
	}

	public static IClasspathEntry[] listClasspathEntriesWithPathMatching(IJavaProject project, Pattern pattern, IProgressMonitor monitor) {
		List<IClasspathEntry> result = new ArrayList<IClasspathEntry>();
		for (IClasspathEntry classpathEntry : getRawClasspath(project)) {
			Matcher matcher = pattern.matcher(classpathEntry.getPath().toPortableString());
			if (matcher.matches()) {
				result.add(classpathEntry);
			}
		}
		return result.toArray(new IClasspathEntry[0]);
	}

	public static IClasspathEntry[] listClasspathEntriesWithPathNotMatching(IJavaProject project, Pattern pattern, IProgressMonitor monitor) {
		List<IClasspathEntry> result = new ArrayList<IClasspathEntry>();
		for (IClasspathEntry classpathEntry : getRawClasspath(project)) {
			Matcher matcher = pattern.matcher(classpathEntry.getPath().toPortableString());
			if (!matcher.matches()) {
				result.add(classpathEntry);
			}
		}
		return result.toArray(new IClasspathEntry[0]);
	}

	public static void removeClasspathEntriesWithPathMatching(IJavaProject project, Pattern pattern, IProgressMonitor monitor) {
		IClasspathEntry[] notMatchingClasspathEntries = listClasspathEntriesWithPathNotMatching(project, pattern, monitor);
		try {
			updateRawClasspath(project, Arrays.asList(notMatchingClasspathEntries), monitor);
		} catch (JavaModelException e) {
			throw new JavaProjectInteractionException(e);
		}
	}
}
