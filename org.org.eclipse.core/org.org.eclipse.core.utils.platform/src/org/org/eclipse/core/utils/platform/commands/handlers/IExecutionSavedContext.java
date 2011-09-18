package org.org.eclipse.core.utils.platform.commands.handlers;

public interface IExecutionSavedContext {
	public void saveParameter(IExecutionSavedParameter executionSavedParameter);

	public boolean hasParameter(String parameterId);

	public IExecutionSavedParameter getParameter(String parameterId);

	public void clearParameter(String parameterId);

	public void clearAllParameters();

	public void dispose();
}
