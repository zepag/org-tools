package org.org.eclipse.core.utils.platform.commands.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.org.eclipse.core.utils.platform.commands.handlers.AbstractExtendedHandler.ExtendedHandlerExecutionException;

public interface IExtendedHandler<T> extends IHandler {

	public T doExecute(ExecutionEvent executionEvent, IExecutionSavedContext executionSavedContext) throws ExtendedHandlerExecutionException;

	public abstract IExecutionBeforeInterceptor initBeforeInterceptor();

	public abstract IExecutionAfterInterceptor<T> initAfterInterceptor();

	public IExecutionSavedContext initExecutionSavedContext();

	public void handleError(ExecutionEvent executionEvent, ExtendedHandlerExecutionException e);
}
