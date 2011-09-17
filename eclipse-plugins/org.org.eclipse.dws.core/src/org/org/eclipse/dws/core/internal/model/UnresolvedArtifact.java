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

import org.org.model.IModelItem;


/**
 * The Class UnresolvedArtifact.
 */
public class UnresolvedArtifact extends AbstractChosenArtifactVersion {
    
    /** The unresolved pom dependency. */
    private PomDependency unresolvedPomDependency;

    /**
     * Gets the unresolved pom dependency.
     * 
     * @return the unresolved pom dependency
     */
    public PomDependency getUnresolvedPomDependency() {
        return unresolvedPomDependency;
    }

    /**
     * Sets the unresolved pom dependency.
     * 
     * @param unresolvedPomDependency the new unresolved pom dependency
     */
    public void setUnresolvedPomDependency(PomDependency unresolvedPomDependency) {
        this.unresolvedPomDependency = unresolvedPomDependency;
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
        return unresolvedPomDependency.getArtifactId() + "-" + unresolvedPomDependency.getVersion() + "<" + scope + ">";
    }

    /* (non-Javadoc)
     * @see org.org.model.AbstractModelItem#getUID()
     */
    /**
     * @see org.org.model.AbstractModelItem#getUID()
     */
    @Override
    public String getUID() {
        return unresolvedPomDependency.getUID();
    }

    /* (non-Javadoc)
     * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
     */
    /**
     * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
     */
    @Override
    public StringBuilder toStringBuilderDescription() {
        return new StringBuilder(unresolvedPomDependency.getUID());
    }
}
