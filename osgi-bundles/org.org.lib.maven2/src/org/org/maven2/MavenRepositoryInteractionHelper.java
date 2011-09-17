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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.Proxy;
import java.net.URL;

import org.org.repository.crawler.maven2.model.Artifact;
import org.org.repository.crawler.maven2.model.ArtifactVersion;
import org.org.repository.crawler.maven2.model.Group;

/**
 * @author pagregoire
 */
public final class MavenRepositoryInteractionHelper {

	private MavenRepositoryInteractionHelper() {
	}

	public static class MavenRepositoryInteractionException extends RuntimeException {

		private static final long serialVersionUID = 7995359925046848534L;

		public MavenRepositoryInteractionException() {
			super();
		}

		public MavenRepositoryInteractionException(String message, Throwable cause) {
			super(message, cause);
		}

		public MavenRepositoryInteractionException(String message) {
			super(message);
		}

		public MavenRepositoryInteractionException(Throwable cause) {
			super(cause);
		}

	}

	public static class MavenRepositoryInteractionEvent implements Serializable {

		private static final long serialVersionUID = 8834580077242700746L;

		public enum Type {
			START_TASK, STOP_TASK
		}

		private final Type eventType;

		private final String message;

		public MavenRepositoryInteractionEvent(Type eventType, String message) {
			super();
			this.eventType = eventType;
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public Type getEventType() {
			return eventType;
		}

	}

	private static InputStream open(URL url, Proxy proxy) throws IOException {
		InputStream inputStream = null;
		if (proxy != null) {
			inputStream = url.openConnection(proxy).getInputStream();
		} else {
			inputStream = url.openConnection().getInputStream();
		}
		return inputStream;
	}

	private static void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e1) {
				// ignore.
			}
		}
	}

	public static DownloadedFilesWrapper downloadArtifactToLocalRepository(ArtifactVersion artifactVersion, File repositoryFolder, Proxy proxy, IEventCallback eventCallback) {
		File artifactVersionFile = null;
		File pomFile = null;
		File javadocFile = null;
		File sourcesFile = null;
		Artifact artifact = artifactVersion.getParent();
		Group group = artifact.getParent();
		try {
			String sGroupFolder = group.getName();
			sGroupFolder = sGroupFolder.replace('.', '/');
			File groupFolder = new File(repositoryFolder, sGroupFolder);
			File artifactFolder = new File(groupFolder, artifact.getId());
			File artifactVersionFolder = new File(artifactFolder, artifactVersion.getVersion());
			if (!artifactVersionFolder.exists()) {
				artifactVersionFolder.mkdirs();
			}
			artifactVersionFile = new File(artifactVersionFolder, artifactVersion.getId());
			URL artifactVersionURL = artifactVersion.getUrl();
			eventCallback.onEvent(new MavenRepositoryInteractionEvent(MavenRepositoryInteractionEvent.Type.START_TASK, "Downloading artifact " + artifactVersionURL));
			downloadFile(artifactVersionFile, proxy, artifactVersionURL);
			eventCallback.onEvent(new MavenRepositoryInteractionEvent(MavenRepositoryInteractionEvent.Type.STOP_TASK, "Artifact " + artifactVersionURL + " downloaded."));
			if (artifactVersion.getPomUrl() != null) {
				try {
					pomFile = new File(artifactVersionFolder, artifactVersion.getParent().getUID() + "-" + artifactVersion.getVersion() + ".pom");
					URL pomURL = artifactVersion.getPomUrl();
					eventCallback.onEvent(new MavenRepositoryInteractionEvent(MavenRepositoryInteractionEvent.Type.START_TASK, "Downloading pom " + pomURL));
					downloadFile(pomFile, proxy, pomURL);
					eventCallback.onEvent(new MavenRepositoryInteractionEvent(MavenRepositoryInteractionEvent.Type.STOP_TASK, "Pom " + artifactVersionURL + " downloaded."));
				} catch (IOException ioe) {
					// FIXME do nothing for now...not being able to retrieve a pom is not a real issue...at this point ;)
				}
			}
			if (artifactVersion.getJavadocUrl() != null) {
				try {
					javadocFile = new File(artifactVersionFolder, artifactVersion.getParent().getUID() + "-" + artifactVersion.getVersion() + "-javadoc.jar");
					URL javadocURL = artifactVersion.getJavadocUrl();
					eventCallback.onEvent(new MavenRepositoryInteractionEvent(MavenRepositoryInteractionEvent.Type.START_TASK, "Downloading javadoc " + javadocURL));
					downloadFile(javadocFile, proxy, javadocURL);
					eventCallback.onEvent(new MavenRepositoryInteractionEvent(MavenRepositoryInteractionEvent.Type.STOP_TASK, "Javadoc " + artifactVersionURL + " downloaded."));
				} catch (IOException ioe) {
					// FIXME do nothing for now...not being able to retrieve a javadoc is not a real issue...at this point ;)
				}
			}
			if (artifactVersion.getSourcesUrl() != null) {
				try {
					sourcesFile = new File(artifactVersionFolder, artifactVersion.getParent().getUID() + "-" + artifactVersion.getVersion() + "-sources.jar");
					URL sourcesURL = artifactVersion.getSourcesUrl();
					eventCallback.onEvent(new MavenRepositoryInteractionEvent(MavenRepositoryInteractionEvent.Type.START_TASK, "Downloading sources " + sourcesURL));
					downloadFile(sourcesFile, proxy, sourcesURL);
					eventCallback.onEvent(new MavenRepositoryInteractionEvent(MavenRepositoryInteractionEvent.Type.STOP_TASK, "Sources " + artifactVersionURL + " downloaded."));
				} catch (IOException ioe) {
					// FIXME do nothing for now...not being able to retrieve sources is not a real issue...at this point ;)
				}
			}
		} catch (IOException e) {
			throw new MavenRepositoryInteractionException("Impossible to download " + artifactVersion.getId() + " to " + artifactVersionFile.getAbsolutePath() + "  : possibly a missing proxy or a wrong host? " + e.getMessage(), e);
		}
		return new DownloadedFilesWrapper(artifactVersionFile, pomFile, javadocFile, sourcesFile);
	}

	private static void downloadFile(File artifactVersionFile, Proxy proxy, URL artifactVersionURL) throws IOException {
		if (!artifactVersionFile.exists()) {
			downloadToLocalFile(artifactVersionFile, artifactVersionURL, proxy);
		}
	}

	/**
	 * @param targetFile
	 * @param requestedURL
	 * @param proxy
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void downloadToLocalFile(File targetFile, URL requestedURL, Proxy proxy) throws IOException, FileNotFoundException {
		InputStream is = open(requestedURL, proxy);
		targetFile.createNewFile();
		FileOutputStream out = new FileOutputStream(targetFile);
		byte[] buf = new byte[1024]; // 1K buffer
		int bytesRead;
		while ((bytesRead = is.read(buf)) != -1) {
			out.write(buf, 0, bytesRead);
		}
		close(is);
		out.close();
	}
}
