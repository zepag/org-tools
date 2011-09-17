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

import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;


/**
 * This is a common abstract class allowing the comparison of ArtifactVersions. Note that the only common thing about these artifactVersions is their scope.
 * 
 * @author pagregoire
 */
@SuppressWarnings("unchecked")
public abstract class AbstractChosenArtifactVersion extends AbstractModelItem<IModelItem, AbstractChosenArtifactVersion> {

	/**
	 * Instantiates a new abstract chosen artifact version.
	 */
	public AbstractChosenArtifactVersion() {

	}

	/** The scope. */
	Scope scope;

	/** The optional. */
	Boolean optional;

	/** The skipped. */
	Boolean skipped = new Boolean(false);

	/** The system path. */
	String systemPath;

	/**
	 * Gets the optional.
	 * 
	 * @return the optional
	 */
	public Boolean getOptional() {
		return optional;
	}

	/**
	 * Sets the optional.
	 * 
	 * @param optional the new optional
	 */
	public void setOptional(Boolean optional) {
		this.optional = optional;
	}

	/**
	 * Gets the scope.
	 * 
	 * @return the scope
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * Sets the scope.
	 * 
	 * @param scope the new scope
	 */
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	/**
	 * Do compare.
	 * 
	 * @param thisObject the this object
	 * @param toCompare the to compare
	 * 
	 * @return the int
	 */
	protected int doCompare(AbstractChosenArtifactVersion thisObject, AbstractChosenArtifactVersion toCompare) {
		// compare artifact versions
		if (thisObject instanceof ResolvedArtifact && toCompare instanceof ResolvedArtifact) {
			ResolvedArtifact thisResolvedArtifact = (ResolvedArtifact) thisObject;
			ResolvedArtifact otherResolvedArtifact = (ResolvedArtifact) toCompare;
			if (thisResolvedArtifact.getArtifactVersion() != null && otherResolvedArtifact.getArtifactVersion() != null) {
				// if other object has an artifact version, compare them
				int artifactResult = thisResolvedArtifact.getArtifactVersion().compareTo(otherResolvedArtifact.getArtifactVersion());
				if (artifactResult == 0) {
					if (thisResolvedArtifact.scope != null) {
						return thisResolvedArtifact.scope.compareTo(otherResolvedArtifact.scope);
					}
				} else {
					return artifactResult;
				}
			} else {
				// if objects have no ArtifactVersion...they are equal..ly wrong
				return 0;
			}
		} else if (thisObject instanceof UnresolvedArtifact && toCompare instanceof UnresolvedArtifact) {
			UnresolvedArtifact thisUnresolvedArtifact = (UnresolvedArtifact) thisObject;
			UnresolvedArtifact otherUnresolvedArtifact = (UnresolvedArtifact) toCompare;
			if (thisUnresolvedArtifact.getUnresolvedPomDependency() != null && otherUnresolvedArtifact.getUnresolvedPomDependency() != null) {
				int artifactResult = thisUnresolvedArtifact.getUnresolvedPomDependency().compareTo(otherUnresolvedArtifact.getUnresolvedPomDependency());
				if (artifactResult == 0) {
					return thisUnresolvedArtifact.scope.compareTo(thisUnresolvedArtifact.scope);
				} else {
					return artifactResult;
				}
			} else {
				// if objects have no PomDependency...they are equal..ly wrong
				return 0;
			}
		} else if (thisObject instanceof UnresolvedArtifact && toCompare instanceof ResolvedArtifact) {
			return -1;
		} else if (thisObject instanceof ResolvedArtifact && toCompare instanceof UnresolvedArtifact) {
			return +1;
		}
		// if nothing happened before...they are equal..ly wrong
		return 0;
	}

	/**
	 * Checks if is skipped.
	 * 
	 * @return the boolean
	 */
	public Boolean isSkipped() {
		return skipped;
	}

	/**
	 * Sets the skipped.
	 * 
	 * @param skipped the new skipped
	 */
	public void setSkipped(Boolean skipped) {
		this.skipped = skipped;
	}
	
	/**
	 * Sets the system path.
	 * 
	 * @param systemPath the new system path
	 */
	public void setSystemPath(String systemPath) {
		this.systemPath=systemPath;
	}

	/**
	 * Gets the system path.
	 * 
	 * @return the system path
	 */
	public String getSystemPath() {
		return systemPath;
	}

    /**
     * This method determines if a chosen artifact's scope is unknown or restricted.<br>
     * The concept of restricted is introduced here for scopes PROVIDED, SYSTEM or RUNTIME.
     * 
     * @return true, if checks if is narrow scope
     */
	public Boolean isNarrowScope() {
		Boolean result = false;
		if (getScope() != null) {
			Scope testedScope=getScope();
			boolean notAScope = testedScope != Scope.PROVIDED; 
			notAScope = notAScope && (testedScope != Scope.SYSTEM); 
			notAScope = notAScope && (testedScope != Scope.RUNTIME); 
			notAScope = notAScope && (testedScope != Scope.TEST); 
			notAScope = notAScope && (testedScope != Scope.COMPILE); 
			boolean narrowScope = testedScope == Scope.PROVIDED; 
			narrowScope = narrowScope || (testedScope == Scope.SYSTEM); 
			narrowScope = narrowScope || (testedScope == Scope.RUNTIME); 
			result = notAScope || narrowScope;
		}
		return result;
	}
}