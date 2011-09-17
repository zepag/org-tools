/**
 * 
 */
package org.org.eclipse.dws.core.internal.jobs;

public enum LibraryDownloadTargetType {
	/** All libraries will be downloaded to local repository and added to classpath. */
	CLASSPATH,
	/** All libraries will be downloaded to local repository if scope is compile, and added to WEB-INF/lib otherwise. */
	WEBINFLIB,
	/** All libraries will be downloaded to a project folder. */
	FOLDER
}