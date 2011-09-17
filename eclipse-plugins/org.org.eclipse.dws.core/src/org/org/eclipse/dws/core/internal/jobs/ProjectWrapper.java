package org.org.eclipse.dws.core.internal.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class ProjectWrapper {
	private final IProject project;

	public ProjectWrapper(IProject project) {
		super();
		this.project = project;
	}

	public IProject getProject() {
		return project;
	}

	public IJavaProject getJavaProject() {
		return JavaCore.create(project);
	}

	public Boolean isFacetedProject() {
		if (!project.exists()) {
			throw new IllegalStateException("This should not be invoked on a non-existing project");
		}
		if (!project.isOpen()) {
			throw new IllegalStateException("This should not be invoked on a closed project");
		}
		boolean hasNature = false;
		try {
			hasNature = project.hasNature("org.eclipse.wst.common.project.facet.core.nature");
		} catch (CoreException e) {
			// Should not occur given the pre-conditions... though...
			hasNature = false;
		}
		return hasNature;
	}

	public String getName() {
		return project.getName();
	}
}