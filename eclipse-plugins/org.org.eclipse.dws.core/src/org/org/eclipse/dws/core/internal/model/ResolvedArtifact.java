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
package org.org.eclipse.dws.core.internal.model;

import java.util.HashSet;
import java.util.Set;

import org.org.model.IModelItem;
import org.org.repository.crawler.maven2.model.ArtifactVersion;


/**
 * The Class ResolvedArtifact.
 */
public class ResolvedArtifact extends AbstractChosenArtifactVersion {

    /** The artifact version. */
    private ArtifactVersion artifactVersion;

    /** The conflicting classpath entries. */
    private Set<DWSClasspathEntryDescriptor> conflictingClasspathEntries;

    /**
     * Gets the artifact version.
     * 
     * @return the artifact version
     */
    public ArtifactVersion getArtifactVersion() {
        return artifactVersion;
    }

    /**
     * Sets the artifact version.
     * 
     * @param artifactVersion the new artifact version
     */
    public void setArtifactVersion(ArtifactVersion artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    /* (non-Javadoc)
     * @see org.org.model.AbstractModelItem#compareTo(org.org.model.IModelItem)
     */
    /**
     * @see org.org.model.AbstractModelItem#compareTo(org.org.model.IModelItem)
     */
    @Override
	@SuppressWarnings("unchecked")
	public int compareTo(IModelItem o) {
        return doCompare(this, (AbstractChosenArtifactVersion) o);
    }

    /* (non-Javadoc)
     * @see org.org.model.AbstractModelItem#toString()
     */
    /**
     * @see org.org.model.AbstractModelItem#toString()
     */
    @Override
    public String toString() {
        return artifactVersion.getId() + "<" + scope + ">";
    }

    /**
     * Gets the conflicting classpath entries.
     * 
     * @return the conflicting classpath entries
     */
    public Set<DWSClasspathEntryDescriptor> getConflictingClasspathEntries() {
        return conflictingClasspathEntries;
    }

    /**
     * Sets the conflicting classpath entries.
     * 
     * @param conflictingClasspathEntries the new conflicting classpath entries
     */
    public void setConflictingClasspathEntries(Set<DWSClasspathEntryDescriptor> conflictingClasspathEntries) {
        this.conflictingClasspathEntries = conflictingClasspathEntries;
    }

    /**
     * Checks for conflicting classpath entries.
     * 
     * @return true, if successful
     */
    public boolean hasConflictingClasspathEntries() {
        return conflictingClasspathEntries != null && conflictingClasspathEntries.size() > 0;
    }

    /**
     * Gets the transitive dependencies.
     * 
     * @return the transitive dependencies
     */
    @SuppressWarnings("unchecked")
	public Set<AbstractChosenArtifactVersion> getTransitiveDependencies() {
        Set<AbstractChosenArtifactVersion> transitiveDependencies = new HashSet<AbstractChosenArtifactVersion>();
        for (IModelItem transitiveDependency : getChildren()) {
            transitiveDependencies.add((AbstractChosenArtifactVersion) transitiveDependency);
        }
        return transitiveDependencies;
    }

    /**
     * Checks for transitive dependencies.
     * 
     * @return true, if successful
     */
    public boolean hasTransitiveDependencies() {
        return hasChildren();
    }

    /* (non-Javadoc)
     * @see org.org.model.AbstractModelItem#getUID()
     */
    /**
     * @see org.org.model.AbstractModelItem#getUID()
     */
    @Override
    public String getUID() {
        return artifactVersion.getUID();
    }

    /* (non-Javadoc)
     * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
     */
    /**
     * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
     */
    @Override
    public StringBuilder toStringBuilderDescription() {
        return new StringBuilder(artifactVersion.getUID());
    }
}
