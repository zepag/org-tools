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
package org.org.eclipse.dws.core.internal;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.org.eclipse.dws.core.internal.model.AbstractChosenArtifactVersion;
import org.org.eclipse.dws.core.internal.model.DWSClasspathEntryDescriptor;
import org.org.eclipse.dws.core.internal.model.ResolvedArtifact;
import org.org.eclipse.dws.core.internal.model.UnresolvedArtifact;
import org.org.eclipse.dws.core.internal.model.PomDependency.Scope;
import org.org.model.IModelItem;
import org.org.model.IModelItemVisitor;
import org.org.repository.crawler.maven2.model.ArtifactVersion;


/**
 * The Class DependenciesHelper.
 */
public class DependenciesHelper {
	
	/**
	 * The Class SearchContext.
	 */
	public static class SearchContext {
		
		/** The deal with transitive. */
		private Boolean dealWithTransitive;

		/** The deal with optional. */
		private Boolean dealWithOptional;

		/** The deal with unknown or restrictive scope. */
		private Boolean dealWithUnknownOrRestrictiveScope;

		/**
		 * Gets the deal with optional.
		 * 
		 * @return the deal with optional
		 */
		public Boolean getDealWithOptional() {
			return dealWithOptional;
		}

		/**
		 * Sets the deal with optional.
		 * 
		 * @param dealWithOptional the new deal with optional
		 */
		public void setDealWithOptional(Boolean dealWithOptional) {
			this.dealWithOptional = dealWithOptional;
		}

		/**
		 * Gets the deal with transitive.
		 * 
		 * @return the deal with transitive
		 */
		public Boolean getDealWithTransitive() {
			return dealWithTransitive;
		}

		/**
		 * Sets the deal with transitive.
		 * 
		 * @param dealWithTransitive the new deal with transitive
		 */
		public void setDealWithTransitive(Boolean dealWithTransitive) {
			this.dealWithTransitive = dealWithTransitive;
		}

		/**
		 * Gets the deal with unknown or restrictive scope.
		 * 
		 * @return the deal with unknown or restrictive scope
		 */
		public Boolean getDealWithUnknownOrRestrictiveScope() {
			return dealWithUnknownOrRestrictiveScope;
		}

		/**
		 * Sets the deal with unknown or restrictive scope.
		 * 
		 * @param dealWithUnknownOrRestrictiveScope the new deal with unknown or restrictive scope
		 */
		public void setDealWithUnknownOrRestrictiveScope(Boolean dealWithUnknownOrRestrictiveScope) {
			this.dealWithUnknownOrRestrictiveScope = dealWithUnknownOrRestrictiveScope;
		}

	}

	/**
	 * The Class LookForUnresolvedArtifactVisitor.
	 */
	private static final class LookForUnresolvedArtifactVisitor implements IModelItemVisitor {
		
		/** The has unresolved artifact. */
		private Boolean hasUnresolvedArtifact=new Boolean(false);

		/**
		 * Instantiates a new look for unresolved artifact visitor.
		 */
		private LookForUnresolvedArtifactVisitor() {
		}

		/* (non-Javadoc)
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		
		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			if (modelItem instanceof UnresolvedArtifact) {
				hasUnresolvedArtifact = true;
				return false;
			}
			return true;
		}

		/**
		 * Found unresolved artifact.
		 * 
		 * @return true, if successful
		 */
		public boolean foundUnresolvedArtifact() {
			return hasUnresolvedArtifact;
		}
	}

	/**
	 * The Class HarvestConflictingClasspathEntriesVisitor.
	 */
	private static class HarvestConflictingClasspathEntriesVisitor implements IModelItemVisitor {
		
		/** The result. */
		private List<DWSClasspathEntryDescriptor> result;

		/**
		 * Instantiates a new harvest conflicting classpath entries visitor.
		 */
		public HarvestConflictingClasspathEntriesVisitor() {
			this.result = new LinkedList<DWSClasspathEntryDescriptor>();
		}

		/**
		 * Gets the result.
		 * 
		 * @return the result
		 */
		public List<DWSClasspathEntryDescriptor> getResult() {
			return result;
		}

		/* (non-Javadoc)
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			if (modelItem instanceof ResolvedArtifact) {
				ResolvedArtifact resolvedArtifact = (ResolvedArtifact) modelItem;
				if (resolvedArtifact.hasConflictingClasspathEntries()) {
					result.addAll(resolvedArtifact.getConflictingClasspathEntries());
				}
			}
			return true;
		}
	}

	/**
	 * The Class RemoveOptionalAndSkippedVisitor.
	 */
	private static class RemoveOptionalAndSkippedVisitor implements IModelItemVisitor {

		/** The search context. */
		private final SearchContext searchContext;

		/**
		 * Instantiates a new removes the optional and skipped visitor.
		 * 
		 * @param searchContext the search context
		 */
		public RemoveOptionalAndSkippedVisitor(SearchContext searchContext) {
			this.searchContext = searchContext;
		}

		/* (non-Javadoc)
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			boolean result = true;
			if (modelItem instanceof AbstractChosenArtifactVersion && modelItem.getParent() != null) {
				AbstractChosenArtifactVersion chosenArtifactVersion = (AbstractChosenArtifactVersion) modelItem;
				final boolean optionalLibrary = chosenArtifactVersion.getOptional();
				if (chosenArtifactVersion.isSkipped() || (optionalLibrary && !(optionalLibrary && searchContext.getDealWithOptional()))) {
					chosenArtifactVersion.getParent().removeChild(chosenArtifactVersion.getUID());
					result = false;
				}
			}
			return result;
		}
	}

	/**
	 * The Class LookForConflictingClasspathEntriesVisitor.
	 */
	private static class LookForConflictingClasspathEntriesVisitor implements IModelItemVisitor {
		
		/** The has at least one conflicting classpath entry. */
		private Boolean hasAtLeastOneConflictingClasspathEntry;

		/**
		 * Instantiates a new look for conflicting classpath entries visitor.
		 */
		public LookForConflictingClasspathEntriesVisitor() {
			this.hasAtLeastOneConflictingClasspathEntry = false;
		}

		/**
		 * Found at least one conflicting classpath entry.
		 * 
		 * @return the boolean
		 */
		public Boolean foundAtLeastOneConflictingClasspathEntry() {
			return hasAtLeastOneConflictingClasspathEntry;
		}

		/* (non-Javadoc)
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			if (modelItem instanceof ResolvedArtifact) {
				if (((ResolvedArtifact) modelItem).hasConflictingClasspathEntries()) {
					hasAtLeastOneConflictingClasspathEntry = true;
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * The Class LookForDuplicateVisitor.
	 */
	private static class LookForDuplicateVisitor implements IModelItemVisitor {

		/** The comparison string. */
		private final String comparisonString;

		/** The has at least one duplicate. */
		private boolean hasAtLeastOneDuplicate;

		/**
		 * Instantiates a new look for duplicate visitor.
		 * 
		 * @param comparisonString the comparison string
		 */
		public LookForDuplicateVisitor(String comparisonString) {
			this.comparisonString = comparisonString;
		}

		/**
		 * Found at least one duplicate.
		 * 
		 * @return true, if successful
		 */
		public boolean foundAtLeastOneDuplicate() {
			return hasAtLeastOneDuplicate;
		}

		/* (non-Javadoc)
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		/**
		 * @see org.org.model.IModelItemVisitor#visit(org.org.model.IModelItem)
		 */
		@SuppressWarnings("rawtypes")
		public boolean visit(IModelItem modelItem) {
			if (modelItem instanceof ResolvedArtifact) {
				ArtifactVersion artifactVersion2 = ((ResolvedArtifact) modelItem).getArtifactVersion();
				String comparisonString2 = buildComparisonString(artifactVersion2);
				if (comparisonString.equals(comparisonString2)) {
					hasAtLeastOneDuplicate = true;
					return false;
				}
			} else {
				return false;
			}
			return true;
		}
	}

	/**
	 * Builds the comparison string.
	 * 
	 * @param artifactVersion the artifact version
	 * 
	 * @return the string
	 */
	private static String buildComparisonString(ArtifactVersion artifactVersion) {
		return artifactVersion.getParent().getParent().getName() + ":" + artifactVersion.getParent().getId(); //$NON-NLS-1$
	}

	/**
	 * Scan resolved artifact for conflicting classpath entries.
	 * 
	 * @param resolvedArtifact the resolved artifact
	 * 
	 * @return the boolean
	 */
	private static Boolean scanResolvedArtifactForConflictingClasspathEntries(ResolvedArtifact resolvedArtifact) {
		LookForConflictingClasspathEntriesVisitor visitor = new LookForConflictingClasspathEntriesVisitor();
		resolvedArtifact.accept(visitor);
		return visitor.foundAtLeastOneConflictingClasspathEntry();
	}

	/**
	 * Extract conflicting classpath entries from resolved artifact.
	 * 
	 * @param resolvedArtifact the resolved artifact
	 * 
	 * @return the list< dws classpath entry descriptor>
	 */
	private static List<DWSClasspathEntryDescriptor> extractConflictingClasspathEntriesFromResolvedArtifact(ResolvedArtifact resolvedArtifact) {
		HarvestConflictingClasspathEntriesVisitor visitor = new HarvestConflictingClasspathEntriesVisitor();
		resolvedArtifact.accept(visitor);
		return visitor.getResult();
	}

	/**
	 * This method checks a set of Chosen artifacts for conflicts.<br>
	 * It returns as soon as it has found a conflict.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return true, if contains conflicting classpath entries
	 */
	public static Boolean containsConflictingClasspathEntries(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		boolean result = false;
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				final boolean optionalLibrary = artifact.getOptional();
				if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
					if (artifact instanceof ResolvedArtifact) {
						boolean containsConflicts = scanResolvedArtifactForConflictingClasspathEntries((ResolvedArtifact) artifact);
						if (containsConflicts) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method looks for any library that has a duplicate.<br>
	 * It returns as soon as it's found a duplicate.
	 * 
	 * @param libraries the libraries
	 * @param transitiveDependencies the transitive dependencies
	 * 
	 * @return true, if contains duplicate libraries
	 */
	public static Boolean containsDuplicateLibraries(List<?> libraries, List<?> transitiveDependencies) {
		for (Object library : libraries) {
			if (library instanceof ResolvedArtifact) {
				ArtifactVersion artifactVersion = ((ResolvedArtifact) library).getArtifactVersion();
				final String comparisonString = buildComparisonString(artifactVersion);
				for (Object transitiveDependency : transitiveDependencies) {
					if (transitiveDependency instanceof ResolvedArtifact) {
						ResolvedArtifact resolvedArtifact = (ResolvedArtifact) transitiveDependency;
						ArtifactVersion artifactVersion2 = ((ResolvedArtifact) transitiveDependency).getArtifactVersion();
						final String comparisonString2 = buildComparisonString(artifactVersion2);
						if (comparisonString.equals(comparisonString2)) {
							return true;
						}
						if (resolvedArtifact.hasChildren()) {
							LookForDuplicateVisitor visitor = new LookForDuplicateVisitor(comparisonString);
							resolvedArtifact.accept(visitor);
							if (visitor.foundAtLeastOneDuplicate()) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * This method extracts the conflicting classpath entries from a list of Chosen artifacts.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return the list< dws classpath entry descriptor>
	 */
	public static List<DWSClasspathEntryDescriptor> extractConflictingClasspathEntries(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		List<DWSClasspathEntryDescriptor> result = new LinkedList<DWSClasspathEntryDescriptor>();
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				final boolean optionalLibrary = artifact.getOptional();
				if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
					if (artifact instanceof ResolvedArtifact) {
						result.addAll(extractConflictingClasspathEntriesFromResolvedArtifact((ResolvedArtifact) artifact));
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method extracts the transitive dependencies for a list of Chosen artifacts.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return the set< abstract chosen artifact version>
	 */
	public static Set<AbstractChosenArtifactVersion> extractTransitiveDependencies(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		Set<AbstractChosenArtifactVersion> result = new LinkedHashSet<AbstractChosenArtifactVersion>();
		for (AbstractChosenArtifactVersion chosenArtifactVersion : chosenArtifactVersions) {
			boolean optional = chosenArtifactVersion.getOptional();
			boolean skipped = chosenArtifactVersion.isSkipped();
			if (!(optional || skipped) || (optional && searchContext.getDealWithOptional())) {
				RemoveOptionalAndSkippedVisitor visitor = new RemoveOptionalAndSkippedVisitor(searchContext);
				chosenArtifactVersion.accept(visitor);
				result.add(chosenArtifactVersion);
			}
		}
		return result;
	}

	/**
	 * This method removes the artifacts with scopes other than COMPILE, TEST or a null scope.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return the set< abstract chosen artifact version>
	 */
	public static Set<AbstractChosenArtifactVersion> filterResolved(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		Set<AbstractChosenArtifactVersion> result = new LinkedHashSet<AbstractChosenArtifactVersion>();
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				final boolean optionalLibrary = artifact.getOptional();
				if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
					if (artifact instanceof ResolvedArtifact) {
						final boolean nullScope = artifact.getScope() == null;
						final boolean compileScope = (!nullScope) && artifact.getScope() == Scope.COMPILE;
						final boolean testScope = (!nullScope) && artifact.getScope() == Scope.TEST;
						if (nullScope || compileScope || testScope) {
							result.add(artifact);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method returns the chosen artifacts which should be added to the Build path in a web application.<br>
	 * This means all libraries except COMPILE Scope libraries or libraries with no scope defined (COMPILE is the default scope),<br>
	 * and except libraries with risky scope.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return the list< abstract chosen artifact version>
	 */
	public static List<AbstractChosenArtifactVersion> filterResolvedForClasspathInWebProjects(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		List<AbstractChosenArtifactVersion> result = new LinkedList<AbstractChosenArtifactVersion>();
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				final boolean optionalLibrary = artifact.getOptional();
				if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
					if (artifact instanceof ResolvedArtifact) {
						if (artifact.getScope() != null && (artifact.getScope() != Scope.COMPILE) && !artifact.isNarrowScope()) { 
							result.add(artifact);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method returns the chosen artifacts which should be added to the /WEB-INF/lib folder in a web application.<br>
	 * This means COMPILE Scope libraries or libraries with no scope defined (COMPILE is the default scope).
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return the list< abstract chosen artifact version>
	 */
	public static List<AbstractChosenArtifactVersion> filterResolvedForWebInfInWebProjects(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		List<AbstractChosenArtifactVersion> result = new LinkedList<AbstractChosenArtifactVersion>();
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				final boolean optionalLibrary = artifact.getOptional();
				if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
					if (artifact instanceof ResolvedArtifact) {
						if (artifact.getScope() == null || artifact.getScope() == Scope.COMPILE) { 
							result.add(artifact);
						}
					}
				}
			}
		}
		return result;

	}

	/**
	 * This method returns the chosen artifacts which have a risky scope.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return the list< abstract chosen artifact version>
	 */
	public static List<AbstractChosenArtifactVersion> filterResolvedWithRiskyScope(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		List<AbstractChosenArtifactVersion> result = new LinkedList<AbstractChosenArtifactVersion>();
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				final boolean optionalLibrary = artifact.getOptional();
				if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
					if (artifact instanceof ResolvedArtifact) {
						if (artifact.isNarrowScope()) {
							result.add(artifact);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method returns the chosen artifacts which are not yet resolved against the available repositories.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return the list< abstract chosen artifact version>
	 */
	public static List<AbstractChosenArtifactVersion> filterUnresolved(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		List<AbstractChosenArtifactVersion> result = new LinkedList<AbstractChosenArtifactVersion>();
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				final boolean optionalLibrary = artifact.getOptional();
				if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
					if (artifact instanceof UnresolvedArtifact) {
						result.add(artifact);
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method is called to determine is any of the chosen artifacts is UnResolved in the available repositories.<br>
	 * It returns as soon as it has found one.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return true, if unresolved libraries
	 */
	public static Boolean unresolvedLibraries(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		boolean result = false;
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				if (artifact instanceof UnresolvedArtifact) {
					final boolean optionalLibrary = artifact.getOptional();
					if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method is called to determine if any of the chosen artifacts or one of its transitive dependencies is UnResolved in the available repositories.<br>
	 * It returns as soon as it has found one.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return true, if unresolved transitive libraries
	 */
	public static Boolean unresolvedTransitiveLibraries(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		boolean result = false;
		if (searchContext.getDealWithTransitive()) {
			for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
				if (artifact.hasChildren()) {
					LookForUnresolvedArtifactVisitor visitor = new LookForUnresolvedArtifactVisitor();
					artifact.accept(visitor);
					if (visitor.foundUnresolvedArtifact()) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method is called to determine if any of the chosen artifacts is Resolved in the available repositories.<br>
	 * It returns as soon as it has found one.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return true, if resolved artifacts
	 */
	public static Boolean resolvedArtifacts(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		Boolean result = false;
		for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
			if (!artifact.isSkipped()) {
				final boolean optionalLibrary = artifact.getOptional();
				if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
					if (artifact instanceof ResolvedArtifact) {
						final boolean nullScope = artifact.getScope() == null;
						final boolean compileScope = (!nullScope) && artifact.getScope() == Scope.COMPILE;
						final boolean testScope = (!nullScope) && artifact.getScope() == Scope.TEST;
						if (nullScope || compileScope || testScope) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method is called to determine if any of the chosen artifacts has transitive dependencies.<br>
	 * It returns as soon as it has found one.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return true, if transitive dependencies
	 */
	public static Boolean transitiveDependencies(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		boolean result = false;
		if (searchContext.getDealWithTransitive()) {
			for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
				if (!artifact.isSkipped()) {
					final boolean optionalLibrary = artifact.getOptional();
					if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
						if (artifact instanceof ResolvedArtifact) {
							ResolvedArtifact resolvedArtifact = (ResolvedArtifact) artifact;
							if (resolvedArtifact.hasTransitiveDependencies()) {
								result = true;
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method is called to determine if any of the chosen artifacts is of unknown or restrictive scope.<br>
	 * It returns as soon as it has found one.
	 * 
	 * @param searchContext the search context
	 * @param chosenArtifactVersions the chosen artifact versions
	 * 
	 * @return true, if unknown or restricted scope
	 */
	public static Boolean unknownOrRestrictedScope(SearchContext searchContext, Set<AbstractChosenArtifactVersion> chosenArtifactVersions) {
		boolean result = false;
		if (searchContext.getDealWithUnknownOrRestrictiveScope()) {
			for (AbstractChosenArtifactVersion artifact : chosenArtifactVersions) {
				if (!artifact.isSkipped()) {
					final boolean optionalLibrary = artifact.getOptional();
					if ((optionalLibrary && searchContext.getDealWithOptional()) || (!optionalLibrary)) {
						if (artifact instanceof ResolvedArtifact) {
							if (artifact.isNarrowScope()) {
								result = true;
								break;
							}
						}
					}
				}
			}
		}
		return result;
	}

}
