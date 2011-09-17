package org.org.eclipse.core.utils.platform.commands.converters;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

public class WorkspaceResourcePathConverter extends AbstractParameterValueConverter {

	@Override
	public final Object convertToObject(final String parameterValue) throws ParameterValueConversionException {
		final Path path = new Path(parameterValue);
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		final IResource resource = workspaceRoot.findMember(path);

		if ((resource == null) || (!resource.exists())) {
			throw new ParameterValueConversionException("parameterValue must be the path of a workspace resource"); //$NON-NLS-1$
		}

		return resource;
	}

	@Override
	public final String convertToString(final Object parameterValue) throws ParameterValueConversionException {
		if (!(parameterValue instanceof IResource)) {
			throw new ParameterValueConversionException("parameterValue must be an " + IResource.class.getName()); //$NON-NLS-1$
		}
		final IResource resource = (IResource) parameterValue;
		return resource.getFullPath().toString();
	}

}
