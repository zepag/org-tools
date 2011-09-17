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
package org.org.eclipse.dws.core.internal.model.visitors;

import java.util.LinkedHashSet;
import java.util.Set;

import org.org.eclipse.dws.core.internal.model.PomDependency;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;


/**
 * The Class DependenciesHarvester.
 */
public class DependenciesHarvester implements IModelItemVisitor {

	/** The dependencies. */
	private Set<PomDependency> dependencies;

	/** The initial dependency. */
	private PomDependency initialDependency;

	/**
	 * Gets the dependencies.
	 * 
	 * @return the dependencies
	 */
	public Set<PomDependency> getDependencies() {
		return dependencies;
	}

	/**
	 * Instantiates a new dependencies harvester.
	 * 
	 * @param transitiveDependency the transitive dependency
	 */
	public DependenciesHarvester(final PomDependency transitiveDependency) {
		this.initialDependency = transitiveDependency;
		this.dependencies = new LinkedHashSet<PomDependency>();
	}

	/* (non-Javadoc)
	 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
	 */
	/**
	 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
	 */
	@SuppressWarnings("unchecked")
	public boolean visit(IModelItem modelItem) {
		if (modelItem.equals(initialDependency)) {
			return true;
		}
		if (modelItem instanceof PomDependency && isScopeCompile((PomDependency) modelItem) && isNotOptional((PomDependency) modelItem)) {
			this.dependencies.add((PomDependency) modelItem);
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Checks if is not optional.
	 * 
	 * @param dependency the dependency
	 * 
	 * @return true, if is not optional
	 */
	private boolean isNotOptional(PomDependency dependency) {
		return !dependency.isOptional();
	}

	/**
	 * Checks if is scope compile.
	 * 
	 * @param modelItem the model item
	 * 
	 * @return true, if is scope compile
	 */
	private boolean isScopeCompile(PomDependency modelItem) {
		return modelItem.getScope() == Scope.COMPILE;
	}
}