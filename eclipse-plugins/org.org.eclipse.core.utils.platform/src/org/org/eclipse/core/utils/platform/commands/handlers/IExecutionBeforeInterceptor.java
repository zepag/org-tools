package org.org.eclipse.core.utils.platform.commands.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler.ExtendedHandlerExecutionException;

public interface IExecutionBeforeInterceptor {
	void beforeExecution(ExecutionEvent executionEvent, IExecutionSavedContext savedContext) throws ExtendedHandlerExecutionException;
}
