package org.org.maven.plugin.dws;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.Restriction;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.filter.AncestorOrSelfDependencyNodeFilter;
import org.apache.maven.shared.dependency.tree.filter.AndDependencyNodeFilter;
import org.apache.maven.shared.dependency.tree.filter.DependencyNodeFilter;
import org.apache.maven.shared.dependency.tree.filter.StateDependencyNodeFilter;
import org.apache.maven.shared.dependency.tree.traversal.BuildingDependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.traversal.CollectingDependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.traversal.FilteringDependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor.TreeTokens;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.org.maven.plugin.dws.utils.OutputUtils;
import org.org.maven.plugin.dws.utils.RepositoryUtils;

/**
 * Retrieves the dependency tree for this project in a flat DWS description.
 * 
 * Note that this code is clearly "inspired" by maven-dependency-plugin version 2.x ;), and also maven-project-report-plugin for the dependencies part.
 * 
 * @author <a href="mailto:pierre.antoine.gregoire@gmail.com">Pierre-Antoine Grégoire</a>
 * @version $Id:$
 * @since 1.0.0
 * @goal descriptor
 * @requiresDependencyResolution test
 */
public class DWSDescriptorMojo extends AbstractMojo implements Contextualizable {
	// fields -----------------------------------------------------------------

	public class CollectingDWSDependencyItemsVisitor implements DependencyNodeVisitor {
		private Set dwsDependencyItems = new TreeSet(new Comparator() {

			public int compare(Object obj1, Object obj2) {
				return obj1.toString().compareTo(obj2.toString());
			}

		});
		private List repositories = new ArrayList();
		private final boolean compileScope;

		private final boolean runtimeScope;

		private final boolean testScope;

		private final boolean providedScope;

		private final boolean systemScope;
		private final RepositoryUtils repositoryUtils;

		public CollectingDWSDependencyItemsVisitor(List repositories, String filteredScope, RepositoryUtils repositoryUtils) {
			getLog().debug("Filtered by " + filteredScope);
			this.repositories = repositories;
			this.repositoryUtils = repositoryUtils;
			if (Artifact.SCOPE_COMPILE.equals(filteredScope)) {
				systemScope = false;
				providedScope = false;
				compileScope = false;
				runtimeScope = true;
				testScope = true;
			} else if (Artifact.SCOPE_RUNTIME.equals(filteredScope)) {
				systemScope = true;
				providedScope = true;
				compileScope = false;
				runtimeScope = false;
				testScope = true;
			} else if (Artifact.SCOPE_TEST.equals(filteredScope)) {
				systemScope = false;
				providedScope = false;
				compileScope = false;
				runtimeScope = false;
				testScope = false;
			} else {
				systemScope = true;
				providedScope = true;
				compileScope = true;
				runtimeScope = true;
				testScope = true;
			}
		}

		public boolean endVisit(DependencyNode dependencyNode) {
			return true;
		}

		public boolean visit(DependencyNode dependencyNode) {
			Artifact artifact = dependencyNode.getArtifact();
			getLog().info(artifact.toString());
			boolean filteredScope = isScopeFiltered(artifact.getScope());
			if (!filteredScope) {
				getLog().debug(dependencyNode.getArtifact() + " is NOT filtered.");
				DWSDependencyItem dwsDependencyItem = new DWSDependencyItem(artifact);
				for (Iterator it2 = repositories.iterator(); it2.hasNext();) {
					ArtifactRepository artifactRepository = (ArtifactRepository) it2.next();
					// String repositoryUrl = artifactRepository.getUrl();
					// String possibleUrl = repositoryUrl + needsSeparator(repositoryUrl) + artifactRepository.pathOf(artifact);
					String possibleUrl = repositoryUtils.getDependencyUrlFromRepository(artifact, artifactRepository);
					if (possibleUrl != null) {
						boolean artifactExists = false;
						// check snapshots in snapshots repository only and releases in release repositories...
						if ((artifact.isSnapshot() && artifactRepository.getSnapshots().isEnabled()) || (!artifact.isSnapshot() && artifactRepository.getReleases().isEnabled())) {
							artifactExists = repositoryUtils.dependencyExistsInRepo(artifactRepository, artifact);
						}
						if (artifactExists) {
							dwsDependencyItem.addArtifactUrl(possibleUrl);
						}
					}
				}
				dwsDependencyItems.add(dwsDependencyItem);
			} else {
				getLog().debug(dependencyNode.getArtifact() + " IS filtered.");
			}
			return (!filteredScope);
		}

		private boolean isScopeFiltered(String artifactScope) {
			boolean filteredScope = false;
			if (artifactScope == null) {
				artifactScope = Artifact.SCOPE_COMPILE;
			}
			if (Artifact.SCOPE_COMPILE.equals(artifactScope)) {
				filteredScope = compileScope;
			} else if (Artifact.SCOPE_RUNTIME.equals(artifactScope)) {
				filteredScope = runtimeScope;
			} else if (Artifact.SCOPE_TEST.equals(artifactScope)) {
				filteredScope = testScope;
			} else if (Artifact.SCOPE_PROVIDED.equals(artifactScope)) {
				filteredScope = providedScope;
			} else if (Artifact.SCOPE_SYSTEM.equals(artifactScope)) {
				filteredScope = systemScope;
			} else {
				filteredScope = true;
			}
			return filteredScope;
		}

		public Set getResult() {
			return dwsDependencyItems;
		}

	}

	/**
	 * The Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * List of Remote Repositories used by the resolver
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @readonly
	 * @required
	 */
	private java.util.List remoteRepositories = Collections.emptyList();

	/**
	 * The artifact repository to use.
	 * 
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
	private ArtifactRepository localRepository;
	//
	// /**
	// * The artifact factory to use.
	// *
	// * @component
	// * @required
	// * @readonly
	// */
	// private ArtifactFactory artifactFactory;

	/**
	 * The artifact metadata source to use.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	private ArtifactMetadataSource artifactMetadataSource;
	//
	// /**
	// * The artifact collector to use.
	// *
	// * @component
	// * @required
	// * @readonly
	// */
	// private ArtifactCollector artifactCollector;

	/**
	 * The dependency tree builder to use.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	private DependencyTreeBuilder dependencyTreeBuilder;

	/**
	 * Wagon manager component.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	private WagonManager wagonManager;

	/**
	 * The current user system settings for use in Maven.
	 * 
	 * @parameter expression="${settings}"
	 * @required
	 * @readonly
	 */
	private Settings settings;

	/**
	 * Maven Project Builder component.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	private MavenProjectBuilder mavenProjectBuilder;

	/**
	 * Artifact Factory component.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	protected ArtifactFactory factory;

	/**
	 * Repository metadata component.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	private RepositoryMetadataManager repositoryMetadataManager;

	/**
	 * Artifact Resolver component.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	protected ArtifactResolver resolver;

	/**
	 * Artifact collector component.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	private ArtifactCollector collector;
	//
	// /**
	// * Jar classes analyzer component.
	// *
	// * @component
	// * @required
	// * @readonly
	// */
	// private JarClassesAnalysis classesAnalyzer;

	/**
	 * If specified, this parameter will cause the dependency tree to be written to the path specified, instead of writing to the console.
	 * 
	 * @since 1.0.0
	 * @parameter expression="${outputFile}" default-value="${basedir}"
	 * 
	 */
	private String outputPath;

	// /**
	// * A regular expression allowing to choose which repositories should be linked and which shouldn't if multiple are available. Use with caution after a first launch, or if you want libraries from some limited repositories. Defaults to all available repositories.
	// *
	// * @since 1.0.0
	// *
	// * @parameter expression="${repos}" default-value=".*"
	// */
	// private String repos;
	/**
	 * The scope to filter by when resolving the dependency tree, or <code>null</code> to include dependencies from all scopes. Note that this feature does not currently work due to MNG-3236.
	 * 
	 * @since 1.0.0
	 * @see <a href="http://jira.codehaus.org/browse/MNG-3236">MNG-3236</a>
	 * 
	 * @parameter expression="${scope}" default-value="compile"
	 */
	private String scope;
	//
	// /**
	// * Runtime Information used to check the Maven version
	// *
	// * @since 1.0.0
	// * @component role="org.apache.maven.execution.RuntimeInformation"
	// */
	// private RuntimeInformation rti;

	/**
	 * The computed dependency tree root node of the Maven project.
	 */
	private DependencyNode rootNode;

	private PlexusContainer container;

	// Mojo methods -----------------------------------------------------------

	/*
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		RepositoryUtils repositoryUtils = new RepositoryUtils(getLog(), container.getLoggerManager(), wagonManager, settings, mavenProjectBuilder, factory, resolver, project.getRemoteArtifactRepositories(), project.getPluginArtifactRepositories(), localRepository, repositoryMetadataManager);

		DependencyNode dependencyTreeNode = resolveProject();

		Set dwsDependencyItems = getDWSDependencyItems(dependencyTreeNode, repositoryUtils);
		try {
			String dependencyTreeString = serialiseDependencyTree(dependencyTreeNode);

			String descriptorContent = createDescriptorContent(dependencyTreeString, dwsDependencyItems);
			if (outputPath != null) {
				File file = new File(outputPath + needsSeparator(outputPath) + "dws.xml");
				OutputUtils.write(descriptorContent, file, getLog());

				getLog().info("Wrote dws descriptor to: " + file);
			} else {
				OutputUtils.log(descriptorContent, getLog());
			}
		} catch (IOException exception) {
			throw new MojoExecutionException("Cannot serialise project dependency tree", exception);
		}
	}

	/**
	 * @return resolve the dependency tree
	 */
	private DependencyNode resolveProject() {
		try {
			ArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_TEST);
			return dependencyTreeBuilder.buildDependencyTree(project, localRepository, factory, artifactMetadataSource, artifactFilter, collector);
		} catch (DependencyTreeBuilderException e) {
			getLog().error("Unable to build dependency tree.", e);
			return null;
		}
	}

	private String createDescriptorContent(String dependencyTreeString, Set dwsDependencyItems) {
		StringBuffer buffer = new StringBuffer();
		final String CRLF = "\n";
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + CRLF);
		buffer.append("<!--" + CRLF);
		buffer.append("\tFULL DEPENDENCY TREE" + CRLF);
		buffer.append(dependencyTreeString + CRLF);
		buffer.append("DEPENDENCIES BELOW ARE FILTERED BY SCOPE: " + scope + CRLF);
		buffer.append("-->" + CRLF);
		buffer.append("<dws>" + CRLF);
		if (dwsDependencyItems.size() > 0) {
			buffer.append("\t<dependencies>" + CRLF);
			for (Iterator it = dwsDependencyItems.iterator(); it.hasNext();) {
				DWSDependencyItem dependencyItem = (DWSDependencyItem) it.next();
				buffer.append("\t\t<dependency>" + CRLF);
				buffer.append("\t\t\t<maven-artifact-description>" + CRLF);
				buffer.append("\t\t\t\t<groupId>" + dependencyItem.getArtifact().getGroupId() + "</groupId>" + CRLF);
				buffer.append("\t\t\t\t<artifactId>" + dependencyItem.getArtifact().getArtifactId() + "</artifactId>" + CRLF);
				buffer.append("\t\t\t\t<version>" + dependencyItem.getArtifact().getVersion() + "</version>" + CRLF);
				buffer.append("\t\t\t\t<classifier>" + dependencyItem.getArtifact().getClassifier() + "</classifier>" + CRLF);
				buffer.append("\t\t\t\t<scope>" + dependencyItem.getArtifact().getScope() + "</scope>" + CRLF);
				buffer.append("\t\t\t\t<type>" + dependencyItem.getArtifact().getType() + "</type>" + CRLF);
				buffer.append("\t\t\t</maven-artifact-description>" + CRLF);
				if (dependencyItem.getArtifactUrls().size() > 0) {
					buffer.append("\t\t\t<download-urls>" + CRLF);
					for (Iterator it2 = dependencyItem.getArtifactUrls().iterator(); it2.hasNext();) {
						String artifactUrl = (String) it2.next();
						buffer.append("\t\t\t\t<download-url>" + artifactUrl + "</download-url>" + CRLF);
					}
					buffer.append("\t\t\t</download-urls>" + CRLF);
				} else {
					buffer.append("\t\t\t<download-urls />" + CRLF);
				}
				buffer.append("\t\t</dependency>" + CRLF);
			}
			buffer.append("\t</dependencies>" + CRLF);
		} else {
			buffer.append("\t<dependencies />" + CRLF);
		}
		buffer.append("</dws>");
		return buffer.toString();
	}

	private Set getDWSDependencyItems(DependencyNode rootNode, RepositoryUtils repositoryUtils) {
		CollectingDWSDependencyItemsVisitor visitor = new CollectingDWSDependencyItemsVisitor(remoteRepositories, scope, repositoryUtils);
		rootNode.accept(visitor);
		return visitor.getResult();
	}

	// public methods ---------------------------------------------------------

	private String needsSeparator(String outputPath) {
		return (outputPath.endsWith("\\") || outputPath.endsWith("/") ? "" : "/");
	}

	/**
	 * Gets the Maven project used by this mojo.
	 * 
	 * @return the Maven project
	 */
	public MavenProject getProject() {
		return project;
	}

	/**
	 * Gets the computed dependency tree root node for the Maven project.
	 * 
	 * @return the dependency tree root node
	 */
	public DependencyNode getDependencyTree() {
		return rootNode;
	}

	// private methods --------------------------------------------------------

	// /**
	// * Gets the artifact filter to use when resolving the dependency tree.
	// *
	// * @return the artifact filter
	// */
	// private ArtifactFilter createResolvingArtifactFilter() {
	// ArtifactFilter filter;
	// // filter filteredScope
	// if (scope != null) {
	// getLog().debug("+ Resolving dependency tree for filteredScope '" + scope + "'");
	// filter = new ScopeArtifactFilter(scope);
	// } else {
	// filter = null;
	// }
	// return filter;
	// }

	/**
	 * Serialises the specified dependency tree to a string.
	 * 
	 * @param rootNode
	 *            the dependency tree root node to serialise
	 * @return the serialised dependency tree
	 */
	private String serialiseDependencyTree(DependencyNode rootNode) {
		StringWriter writer = new StringWriter();
		// standard extended or whitespace
		org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor.TreeTokens treeTokens = toTreeTokens("standard");
		DependencyNodeVisitor visitor = new SerializingDependencyNodeVisitor(writer, treeTokens);
		// TODO: remove the need for this when the serializer can calculate last nodes from visitor calls only
		visitor = new BuildingDependencyNodeVisitor(visitor);

		DependencyNodeFilter filter = createDependencyNodeFilter();

		if (filter != null) {
			CollectingDependencyNodeVisitor collectingVisitor = new CollectingDependencyNodeVisitor();
			DependencyNodeVisitor firstPassVisitor = new FilteringDependencyNodeVisitor(collectingVisitor, filter);
			rootNode.accept(firstPassVisitor);

			DependencyNodeFilter secondPassFilter = new AncestorOrSelfDependencyNodeFilter(collectingVisitor.getNodes());
			visitor = new FilteringDependencyNodeVisitor(visitor, secondPassFilter);
		}

		rootNode.accept(visitor);

		return writer.toString();
	}

	/**
	 * Gets the tree tokens instance for the specified name.
	 * 
	 * @param tokens
	 *            the tree tokens name
	 * @return the <code>TreeTokens</code> instance
	 */
	private org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor.TreeTokens toTreeTokens(String tokens) {
		TreeTokens treeTokens;
		if ("whitespace".equals(tokens)) {
			getLog().debug("+ Using whitespace tree tokens");
			treeTokens = SerializingDependencyNodeVisitor.WHITESPACE_TOKENS;
		} else if ("extended".equals(tokens)) {
			getLog().debug("+ Using extended tree tokens");
			treeTokens = SerializingDependencyNodeVisitor.EXTENDED_TOKENS;
		} else {
			treeTokens = SerializingDependencyNodeVisitor.STANDARD_TOKENS;
		}

		return treeTokens;
	}

	/**
	 * Gets the dependency node filter to use when serializing the dependency tree.
	 * 
	 * @return the dependency node filter, or <code>null</code> if none required
	 */
	private DependencyNodeFilter createDependencyNodeFilter() {
		List filters = new ArrayList();
		getLog().debug("+ Filtering omitted nodes from dependency tree");
		filters.add(StateDependencyNodeFilter.INCLUDED);

		return filters.isEmpty() ? null : new AndDependencyNodeFilter(filters);
	}

	/**
	 * Copied from Artifact.VersionRange. This is tweaked to handle singular ranges properly. Currently the default containsVersion method assumes a singular version means allow everything. This method assumes that "2.0.4" == "[2.0.4,)"
	 * 
	 * @param allowedRange
	 *            range of allowed versions.
	 * @param theVersion
	 *            the version to be checked.
	 * @return true if the version is contained by the range.
	 */
	public static boolean containsVersion(VersionRange allowedRange, ArtifactVersion theVersion) {
		boolean matched = false;
		ArtifactVersion recommendedVersion = allowedRange.getRecommendedVersion();
		if (recommendedVersion == null) {

			for (Iterator i = allowedRange.getRestrictions().iterator(); i.hasNext() && !matched;) {
				Restriction restriction = (Restriction) i.next();
				if (restriction.containsVersion(theVersion)) {
					matched = true;
				}
			}
		} else {
			// only singular versions ever have a recommendedVersion
			int compareTo = recommendedVersion.compareTo(theVersion);
			matched = (compareTo <= 0);
		}
		return matched;
	}

	public void contextualize(Context context) throws ContextException {
		this.container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
	}

}
