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
package org.org.repository.crawler.maven2.model;

import java.net.URL;

import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;

/**
 * @author pagregoire
 */
@SuppressWarnings("rawtypes")
public class ArtifactVersion extends AbstractModelItem<Artifact, IModelItem> {

	public enum Type {
		POM, ARCHIVE, LIBRARY
	}

	private URL url;

	private String id;

	private String version;

	private String classifier;

	private URL pomUrl;

	private URL javadocUrl;

	private URL sourcesUrl;

	private Type type;

	public URL getPomUrl() {
		return this.pomUrl;
	}

	public void setPomUrl(URL pomUrl) {
		this.pomUrl = pomUrl;
	}

	public String getUID() {
		return id + " " + version + (classifier != null ? (" " + classifier) : "");
	}

	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(getUID() + "[" + type.toString() + "]" + (pomUrl == null ? "" : " " + pomUrl) + (javadocUrl == null ? "" : " " + javadocUrl) + (sourcesUrl == null ? "" : " " + sourcesUrl));
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            The version to set.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	public void setJavadocUrl(URL javadocUrl) {
		this.javadocUrl = javadocUrl;
	}

	public URL getJavadocUrl() {
		return javadocUrl;
	}

	public URL getSourcesUrl() {
		return sourcesUrl;
	}

	public void setSourcesUrl(URL sourcesUrl) {
		this.sourcesUrl = sourcesUrl;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}
}
