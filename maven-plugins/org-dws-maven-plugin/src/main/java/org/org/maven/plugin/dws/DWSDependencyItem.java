package org.org.maven.plugin.dws;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

public class DWSDependencyItem {
	private final Artifact artifact;
	private final Set artifactUrls;

	public DWSDependencyItem(Artifact artifact) {
		super();
		this.artifact = artifact;
		this.artifactUrls = new HashSet();
	}

	public void addArtifactUrl(String url) {
		artifactUrls.add(url);
	}

	public Set getArtifactUrls() {
		return artifactUrls;
	}

	public Artifact getArtifact() {
		return artifact;
	}

	public int hashCode() {
		return artifact.toString().hashCode();
	}

	public String toString() {
		return artifact.toString() + " " + artifactUrls.toString();
	}
}