package org.org.eclipse.core.utils.platform.commands.handlers;

import java.util.HashMap;
import java.util.Map;

public class ExecutionSavedContextImpl implements IExecutionSavedContext {
	private Map<String, IExecutionSavedParameter> parameters = new HashMap<String, IExecutionSavedParameter>();

	public void clearAllParameters() {
		parameters.clear();
	}

	public void clearParameter(String parameterId) {
		parameters.remove(parameterId);

	}

	public IExecutionSavedParameter getParameter(String parameterId) {
		return parameters.get(parameterId);
	}

	public boolean hasParameter(String parameterId) {
		return parameters.containsKey(parameterId);
	}

	public void saveParameter(IExecutionSavedParameter executionSavedParameter) {
		parameters.put(executionSavedParameter.getName(), executionSavedParameter);
	}

	public void dispose() {
		// No need for disposal
	}
}