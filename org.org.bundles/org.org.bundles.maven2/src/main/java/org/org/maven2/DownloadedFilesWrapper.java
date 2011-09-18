/*
 org.org.lib.maven2 is a java library/OSGI Bundle
 Providing Maven repository related functionalities.
 Copyright (C) 2007  Pierre-Antoine Grégoire
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.org.maven2;

import java.io.File;
import java.io.Serializable;

public class DownloadedFilesWrapper implements Serializable {

	private static final long serialVersionUID = -5524265154815321419L;

	private final File sourcesFile;

	private final File javadocFile;

	private final File pomFile;

	private final File artifactVersionFile;

	public DownloadedFilesWrapper(File artifactVersionFile, File pomFile, File javadocFile, File sourcesFile) {
		this.artifactVersionFile = artifactVersionFile;
		this.pomFile = pomFile;
		this.javadocFile = javadocFile;
		this.sourcesFile = sourcesFile;
	}

	public File getArtifactVersionFile() {
		return artifactVersionFile;
	}

	public File getJavadocFile() {
		return javadocFile;
	}

	public File getPomFile() {
		return pomFile;
	}

	public File getSourcesFile() {
		return sourcesFile;
	}

}
