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

import java.util.Set;

import org.org.eclipse.dws.core.internal.model.visitors.PomDependenciesFilteringOptions;
import org.org.eclipse.dws.core.internal.model.visitors.PomDependencyTransitiveDependenciesHarvester;
import org.org.model.AbstractModelItem;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;


/**
 * This value object represents a pom.xml file's declaration of a dependency. while parsing is done, related artifact version are attributed to this value object
 * 
 * @author pagregoire
 */
@SuppressWarnings("rawtypes")
public class PomDependency extends AbstractModelItem<IModelItem, PomDependency> {
	

	/**
	 * The Class TransitiveOptionalModelItemVisitor.
	 */
	private final class TransitiveOptionalModelItemVisitor implements IModelItemVisitor {
		
		/** The are all optional. */
		private boolean areAllOptional = true;

		/* (non-Javadoc)
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		public boolean visit(IModelItem modelItem) {
			PomDependency dependency = (PomDependency) modelItem;
			if (!dependency.isOptional()) {
				areAllOptional = false;
				return false;
			} else {
				return true;
			}
		}

		/**
		 * Are all optional.
		 * 
		 * @return true, if successful
		 */
		public boolean areAllOptional() {
			return areAllOptional;
		}
	}

	/**
	 * The Class TransitiveScopeRiskyModelItemVisitor.
	 */
	private final class TransitiveScopeRiskyModelItemVisitor implements IModelItemVisitor {
		
		/** The are all risky. */
		private boolean areAllRisky = true;

		/* (non-Javadoc)
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		public boolean visit(IModelItem modelItem) {
			PomDependency dependency = (PomDependency) modelItem;
			if (!dependency.isScopeNarrow()) {
				areAllRisky = false;
				return false;
			} else {
				return true;
			}
		}

		/**
		 * Are all risky.
		 * 
		 * @return true, if successful
		 */
		public boolean areAllRisky() {
			return areAllRisky;
		}
	}

	/**
	 * The Enum Scope.
	 */
	public enum Scope {
		
		/** The COMPILE. */
		COMPILE, 
 
 /** The RUNTIME. */
 RUNTIME, 
 
 /** The PROVIDED. */
 PROVIDED, 
 
 /** The TEST. */
 TEST, 
 
 /** The SYSTEM. */
 SYSTEM, 
 
 /** The OTHER. */
 OTHER
	}

	/** The scope. */
	private Scope scope;

	/** The group id. */
	private String groupId;

	/** The artifact id. */
	private String artifactId;

	/** The version. */
	private String version;

	/** The classifier. */
	private String classifier;

	/** The system path. */
	private String systemPath;

	/** The optional. */
	private Boolean optional = Boolean.valueOf(false);

	/** The conflicting classpath entries. */
	private Set<DWSClasspathEntryDescriptor> conflictingClasspathEntries;

	// private List<ArtifactVersion> artifactVersionsFromRepositories;

	/**
	 * Gets the artifact id.
	 * 
	 * @return the artifact id
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * Sets the artifact id.
	 * 
	 * @param artifactId the artifact id
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * Gets the group id.
	 * 
	 * @return the group id
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Sets the group id.
	 * 
	 * @param groupId the group id
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
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
	 * @param scope the scope
	 */
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version.
	 * 
	 * @param version the version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Compare to.
	 * 
	 * @param o the o
	 * 
	 * @return the int
	 * 
	 * @see java.lang.Comparable
	 */
	@Override
	public int compareTo(IModelItem o) {
		PomDependency pomDependency = (PomDependency) o;
		return String.CASE_INSENSITIVE_ORDER.compare(groupId + artifactId + version + classifier, pomDependency.groupId + pomDependency.artifactId + pomDependency.version + pomDependency.classifier);
	}

	/**
	 * This method is a getter for the conflicting classpath entries.
	 * 
	 * @return the conflicting classpath entries.
	 */
	public Set<DWSClasspathEntryDescriptor> getConflictingClasspathEntries() {
		return conflictingClasspathEntries;
	}

	/**
	 * This method is a setter for the conflicting classpath entries.
	 * 
	 * @param conflictingClasspathEntries the conflicting classpath entries to set
	 */
	public void setConflictingClasspathEntries(Set<DWSClasspathEntryDescriptor> conflictingClasspathEntries) {
		this.conflictingClasspathEntries = conflictingClasspathEntries;
	}

	/**
	 * This methods tests whether this pom dependency conflicts with classpath entries or not.
	 * 
	 * @return the boolean value of the assertion
	 */
	public boolean isConflictingWithClasspathEntries() {
		return conflictingClasspathEntries != null && conflictingClasspathEntries.size() > 0;
	}

	/**
	 * @see org.org.model.AbstractModelItem#getUID()
	 */
	@Override
	public String getUID() {
		return this.getGroupId() + ":" + this.getArtifactId() + ":" + this.getVersion() + ":" + this.getClassifier() + ":" + this.getScope();
	}

	/**
	 * @see org.org.model.AbstractModelItem#toStringBuilderDescription()
	 */
	@Override
	public StringBuilder toStringBuilderDescription() {
		return new StringBuilder(this.getGroupId() + ":" + this.getArtifactId() + ":" + this.getVersion() + ":" + (this.getClassifier() == null ? "[no_classifier]" : this.getClassifier()) + ":" + this.getScope());
	}

	/**
	 * Checks if is optional.
	 * 
	 * @return the boolean
	 */
	public Boolean isOptional() {
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
	 * Gets the classifier.
	 * 
	 * @return the classifier
	 */
	public String getClassifier() {
		return classifier;
	}

	/**
	 * Sets the classifier.
	 * 
	 * @param classifier the new classifier
	 */
	public void setClassifier(String classifier) {
		this.classifier = classifier;
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
	 * Sets the system path.
	 * 
	 * @param systemPath the new system path
	 */
	public void setSystemPath(String systemPath) {
		this.systemPath = systemPath;
	}

	/**
	 * Checks if is scope narrow.
	 * 
	 * @return true, if is scope narrow
	 */
	public boolean isScopeNarrow() {
		boolean result = false;
		if (this.getScope() != null) {
			boolean notAScope = !(this.getScope().equals(Scope.PROVIDED));
			notAScope = notAScope && (!this.getScope().equals(Scope.SYSTEM));
			notAScope = notAScope && (!this.getScope().equals(Scope.RUNTIME));
			notAScope = notAScope && (!this.getScope().equals(Scope.TEST));
			notAScope = notAScope && (!this.getScope().equals(Scope.COMPILE));
			boolean riskyScope = this.getScope().equals(Scope.PROVIDED);
			riskyScope = riskyScope || (this.getScope().equals(Scope.SYSTEM));
			riskyScope = riskyScope || (this.getScope().equals(Scope.RUNTIME));
			result = notAScope || riskyScope;
		}
		return result;
	}

	/**
	 * Are transitive all narrow scoped.
	 * 
	 * @return true, if successful
	 */
	public boolean areTransitiveAllNarrowScoped() {
		TransitiveScopeRiskyModelItemVisitor transitiveScopeRiskyModelItemVisitor = new TransitiveScopeRiskyModelItemVisitor();
		accept(transitiveScopeRiskyModelItemVisitor);
		return transitiveScopeRiskyModelItemVisitor.areAllRisky();
	}

	/**
	 * Are transitive all optional.
	 * 
	 * @return true, if successful
	 */
	public boolean areTransitiveAllOptional() {
		TransitiveOptionalModelItemVisitor transitiveOptionalModelItemVisitor = new TransitiveOptionalModelItemVisitor();
		accept(transitiveOptionalModelItemVisitor);
		return transitiveOptionalModelItemVisitor.areAllOptional();
	}

	/**
	 * Retrieve transitive dependencies.
	 * 
	 * @param filteringOptions the filtering options
	 */
	public void retrieveTransitiveDependencies(final PomDependenciesFilteringOptions filteringOptions) {
		PomDependencyTransitiveDependenciesHarvester pomDependencyTransitiveDependenciesHarvester = new PomDependencyTransitiveDependenciesHarvester(filteringOptions);
		accept(pomDependencyTransitiveDependenciesHarvester);
	}
}
