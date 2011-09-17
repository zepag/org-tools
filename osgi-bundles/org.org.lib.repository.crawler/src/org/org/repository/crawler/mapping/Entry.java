/*
 org.org.lib.repository.crawler is a java library/OSGI Bundle
 Providing Crawling capabilities for Maven 2 HTTP exposed repositories
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
package org.org.repository.crawler.mapping;

/**
 * @author pagregoire
 */
public class Entry implements Comparable<Entry> {

	private String value;

	private String resolvedName;

	public enum MavenType {
		GROUP_PATH_FOLDER, ARTIFACT_FOLDER, ARTIFACT_VERSION_FOLDER, METADATA_FILE, UNDETERMINED
	}

	public enum RawType {
		DIRECTORY, FILE, UNDETERMINED
	}

	private MavenType mavenType = MavenType.UNDETERMINED;

	private RawType rawType = RawType.UNDETERMINED;

	public Entry(String value, String resolvedName, MavenType mavenType, RawType rawType) {
		super();
		this.value = value;
		this.resolvedName = resolvedName;
		this.mavenType = mavenType;
		this.rawType = rawType;
	}

	public Entry() {
		super();
	}

	public String getResolvedName() {
		return resolvedName;
	}

	public void setResolvedName(String resolvedName) {
		this.resolvedName = resolvedName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "\n[" + rawType + " " + mavenType + " resolvedName=" + resolvedName + ";value=" + value + ";]";
	}

	public int compareTo(Entry toCompare) {

		Entry thisResolvedArtifact = (Entry) this;
		Entry otherResolvedArtifact = (Entry) toCompare;
		if (thisResolvedArtifact.getResolvedName() != null && otherResolvedArtifact.getResolvedName() != null) {
			// if other object has an artifact version, compare them
			int artifactResult = thisResolvedArtifact.getResolvedName().compareTo(otherResolvedArtifact.getResolvedName());
			if (artifactResult == 0) {
				if (thisResolvedArtifact.value != null) {
					return thisResolvedArtifact.value.compareTo(otherResolvedArtifact.value);
				}
			} else {
				return artifactResult;
			}
		} else {
			// if objects have no ArtifactVersion...they are equal..ly wrong
			return 0;
		}
		// if nothing happened before...they are equal..ly wrong
		return 0;
	}

	public MavenType getMavenType() {
		return mavenType;
	}

	public Boolean isMavenType(MavenType mavenType) {
		return this.mavenType == mavenType;
	}

	public void setMavenType(MavenType mavenType) {
		this.mavenType = mavenType;
	}

	public RawType getRawType() {
		return rawType;
	}

	public void setRawType(RawType rawType) {
		this.rawType = rawType;
	}

	public Boolean isRawType(RawType rawType) {
		return this.rawType == rawType;
	}
}
