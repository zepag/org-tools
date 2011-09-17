package org.org.eclipse.core.utils.platform.commands.converters;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

public class UrlConverter extends AbstractParameterValueConverter {

	@Override
	public final Object convertToObject(final String parameterValue) throws ParameterValueConversionException {
		URL url = null;
		try {
			url = new URL(parameterValue);
		} catch (MalformedURLException e) {
			throw new ParameterValueConversionException("parameterValue must be a well-formed URL:" + e.getMessage());
		}

		if (url == null) {
			throw new ParameterValueConversionException("parameterValue must be the path of a workspace resource");
		}
		return url;
	}

	@Override
	public final String convertToString(final Object parameterValue) throws ParameterValueConversionException {
		if (!(parameterValue instanceof URL)) {
			throw new ParameterValueConversionException("parameterValue must be an " + URL.class.getName()); //$NON-NLS-1$
		}
		final URL resource = (URL) parameterValue;
		return resource.toExternalForm();
	}

}