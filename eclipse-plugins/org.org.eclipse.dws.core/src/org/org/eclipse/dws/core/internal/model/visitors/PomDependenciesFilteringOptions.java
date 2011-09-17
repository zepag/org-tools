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

import java.util.Set;

import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;
import org.org.eclipse.dws.core.internal.model.SkippedDependency;


/**
 * The Class PomDependenciesFilteringOptions.
 */
public class PomDependenciesFilteringOptions {

	/**
	 * The Enum Filter.
	 */
	public static enum Filter {

		/** The CONFLICTING. */
		CONFLICTING,
		
		/** The NONE. */
		NONE
	}

	/**
	 * The Enum ScopeFilter.
	 */
	public enum ScopeFilter {

		/** The FILTE r_ narro w_ scopes. */
		FILTER_NARROW_SCOPES,
		
		/** The NONE. */
		NONE
	}

	/** The project classpath entries. */
	private final Set<DWSClasspathEntryDescriptor> projectClasspathEntries;

	/** The scope filter. */
	private final ScopeFilter scopeFilter;

	/** The filter. */
	private final Filter filter;

	/** The artifact extensions. */
	private final Set<String> artifactExtensions;

	/** The skipped dependencies. */
	private final Set<SkippedDependency> skippedDependencies;

	/** The deal with transitive. */
	private final boolean dealWithTransitive;

	/** The deal with narrow. */
	private final boolean dealWithNarrow;

	/** The deal with optional. */
	private final boolean dealWithOptional;

	/**
	 * The Class Builder.
	 */
	public static class Builder {

		/** The project classpath entries. */
		private Set<DWSClasspathEntryDescriptor> projectClasspathEntries;

		/** The scope filter. */
		private ScopeFilter scopeFilter;

		/** The filter. */
		private Filter filter;

		/** The artifact extensions. */
		private Set<String> artifactExtensions;

		/** The skipped dependencies. */
		private Set<SkippedDependency> skippedDependencies;

		/** The deal with transitive. */
		private boolean dealWithTransitive;

		/** The deal with narrow. */
		private boolean dealWithNarrow;

		/** The deal with optional. */
		private boolean dealWithOptional;

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
		}

		/**
		 * Instantiates a new builder.
		 * 
		 * @param filteringOptions the filtering options
		 */
		public Builder(PomDependenciesFilteringOptions filteringOptions) {
			this.projectClasspathEntries = filteringOptions.projectClasspathEntries;
			this.scopeFilter = filteringOptions.scopeFilter;
			this.filter = filteringOptions.filter;
			this.artifactExtensions = filteringOptions.artifactExtensions;
			this.skippedDependencies = filteringOptions.skippedDependencies;
			this.dealWithTransitive = filteringOptions.dealWithTransitive;
			this.dealWithNarrow = filteringOptions.dealWithNarrow;
			this.dealWithOptional = filteringOptions.dealWithOptional;
		}

		/**
		 * Project classpath entries.
		 * 
		 * @param projectClasspathEntries the project classpath entries
		 * 
		 * @return the builder
		 */
		public Builder projectClasspathEntries(Set<DWSClasspathEntryDescriptor> projectClasspathEntries) {
			this.projectClasspathEntries = projectClasspathEntries;
			return this;
		}

		/**
		 * Scope filter.
		 * 
		 * @param scopeFilter the scope filter
		 * 
		 * @return the builder
		 */
		public Builder scopeFilter(ScopeFilter scopeFilter) {
			this.scopeFilter = scopeFilter;
			return this;
		}

		/**
		 * Filter.
		 * 
		 * @param filter the filter
		 * 
		 * @return the builder
		 */
		public Builder filter(Filter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Artifact extensions.
		 * 
		 * @param artifactExtensions the artifact extensions
		 * 
		 * @return the builder
		 */
		public Builder artifactExtensions(Set<String> artifactExtensions) {
			this.artifactExtensions = artifactExtensions;
			return this;
		}

		/**
		 * Skipped dependencies.
		 * 
		 * @param skippedDependencies the skipped dependencies
		 * 
		 * @return the builder
		 */
		public Builder skippedDependencies(Set<SkippedDependency> skippedDependencies) {
			this.skippedDependencies = skippedDependencies;
			return this;
		}

		/**
		 * Deal with transitive.
		 * 
		 * @param dealWithTransitive the deal with transitive
		 * 
		 * @return the builder
		 */
		public Builder dealWithTransitive(boolean dealWithTransitive) {
			this.dealWithTransitive = dealWithTransitive;
			return this;
		}

		/**
		 * Deal with narrow.
		 * 
		 * @param dealWithNarrow the deal with narrow
		 * 
		 * @return the builder
		 */
		public Builder dealWithNarrow(boolean dealWithNarrow) {
			this.dealWithNarrow = dealWithNarrow;
			return this;
		}

		/**
		 * Deal with optional.
		 * 
		 * @param dealWithOptional the deal with optional
		 * 
		 * @return the builder
		 */
		public Builder dealWithOptional(boolean dealWithOptional) {
			this.dealWithOptional = dealWithOptional;
			return this;
		}

		/**
		 * Builds the.
		 * 
		 * @return the filtering options
		 */
		public PomDependenciesFilteringOptions build() {
			return new PomDependenciesFilteringOptions(this);
		}
	}

	/**
	 * Instantiates a new filtering options.
	 * 
	 * @param builder the builder
	 */
	private PomDependenciesFilteringOptions(Builder builder) {
		super();
		this.projectClasspathEntries = builder.projectClasspathEntries;
		this.scopeFilter = builder.scopeFilter;
		this.filter = builder.filter;
		this.artifactExtensions = builder.artifactExtensions;
		this.skippedDependencies = builder.skippedDependencies;
		this.dealWithTransitive = builder.dealWithTransitive;
		this.dealWithNarrow = builder.dealWithNarrow;
		this.dealWithOptional = builder.dealWithOptional;
	}

	/**
	 * Gets the project classpath entries.
	 * 
	 * @return the project classpath entries
	 */
	public Set<DWSClasspathEntryDescriptor> getProjectClasspathEntries() {
		return projectClasspathEntries;
	}

	/**
	 * Gets the scope filter.
	 * 
	 * @return the scope filter
	 */
	public ScopeFilter getScopeFilter() {
		return scopeFilter;
	}

	/**
	 * Gets the filter.
	 * 
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * Gets the artifact extensions.
	 * 
	 * @return the artifact extensions
	 */
	public Set<String> getArtifactExtensions() {
		return artifactExtensions;
	}

	/**
	 * Gets the skipped dependencies.
	 * 
	 * @return the skipped dependencies
	 */
	public Set<SkippedDependency> getSkippedDependencies() {
		return skippedDependencies;
	}

	/**
	 * Deal with transitive.
	 * 
	 * @return true, if successful
	 */
	public boolean dealWithTransitive() {
		return dealWithTransitive;
	}

	/**
	 * Deal with narrow.
	 * 
	 * @return true, if successful
	 */
	public boolean dealWithNarrow() {
		return dealWithNarrow;
	}

	/**
	 * Deal with optional.
	 * 
	 * @return true, if successful
	 */
	public boolean dealWithOptional() {
		return dealWithOptional;
	}

}
