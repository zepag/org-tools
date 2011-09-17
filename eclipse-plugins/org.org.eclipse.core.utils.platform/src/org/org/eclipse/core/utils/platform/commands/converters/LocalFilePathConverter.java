package org.org.eclipse.core.utils.platform.commands.converters;

import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.resources.IResource;

public class LocalFilePathConverter extends AbstractParameterValueConverter {

	@Override
	public final Object convertToObject(final String parameterValue) throws ParameterValueConversionException {
		final File file = new File(parameterValue);
		try {
			file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new ParameterValueConversionException("parameterValue must be the path of a file"); //$NON-NLS-1$
		}
		return file;
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
