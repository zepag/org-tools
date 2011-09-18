package org.org.eclipse.core.utils.platform.commands.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler.ExtendedHandlerExecutionException;

public interface IExecutionAfterInterceptor<T> {
	void afterExecution(ExecutionEvent executionEvent, IExecutionSavedContext savedContext, T result)throws ExtendedHandlerExecutionException;
}
