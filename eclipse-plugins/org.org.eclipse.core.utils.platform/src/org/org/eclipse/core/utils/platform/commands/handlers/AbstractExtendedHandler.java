package org.org.eclipse.core.utils.platform.commands.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.org.eclipse.core.utils.platform.PlatformUtilsPlugin;
import org.org.eclipse.core.utils.platform.dialogs.message.ErrorDialog;

public abstract class AbstractExtendedHandler<T> extends AbstractHandler implements IExtendedHandler<T> {

	private IExecutionBeforeInterceptor beforeInterceptor;
	private IExecutionSavedContext executionSavedContext;
	private IExecutionAfterInterceptor<T> afterInterceptor;

	public static class ExtendedHandlerExecutionException extends RuntimeException {
		private static final long serialVersionUID = 4133062248943748158L;

		public ExtendedHandlerExecutionException() {
			super();
		}

		public ExtendedHandlerExecutionException(String message, Throwable cause) {
			super(message, cause);
		}

		public ExtendedHandlerExecutionException(String message) {
			super(message);
		}

		public ExtendedHandlerExecutionException(Throwable cause) {
			super(cause);
		}
	}

	public AbstractExtendedHandler() {
		this.beforeInterceptor = initBeforeInterceptor();
		this.afterInterceptor = initAfterInterceptor();
		this.executionSavedContext = initExecutionSavedContext();
	}

	@Override
	public final Object execute(ExecutionEvent executionEvent) throws ExecutionException {
		T result = null;
		try {
			beforeInterceptor.beforeExecution(executionEvent, executionSavedContext);
			result = doExecute(executionEvent, executionSavedContext);
			afterInterceptor.afterExecution(executionEvent, executionSavedContext, result);
		} catch (ExtendedHandlerExecutionException e) {
			handleError(executionEvent, e);
		} catch (Throwable e) {
			PlatformUtilsPlugin.getDefault().getLog().log(new Status(Status.ERROR, PlatformUtilsPlugin.PLUGIN_ID, "UNFORESEEN ERROR " + executionEvent.getCommand().getId(), e));
			throw new ExecutionException("Error could not be recovered by the handler", e);
		} finally {
			executionSavedContext.dispose();
		}
		return result;
	}

	public void handleError(ExecutionEvent executionEvent, ExtendedHandlerExecutionException e) {
		PlatformUtilsPlugin.getDefault().getLog().log(new Status(Status.ERROR, PlatformUtilsPlugin.PLUGIN_ID, "ERROR CAUGHT " + executionEvent.getCommand().getId(), e));
		ErrorDialog errorDialog = new ErrorDialog("An error occured while executing command.", e.getMessage() + "\nOpen details to learn more about this issue.", e);
		errorDialog.open();
	}

	public IExecutionBeforeInterceptor initBeforeInterceptor() {
		return new IExecutionBeforeInterceptor() {

			public void beforeExecution(ExecutionEvent executionEvent, IExecutionSavedContext savedContext) {
				if (executionEvent.getCommand() != null) {
					PlatformUtilsPlugin.getDefault().getLog().log(new Status(Status.INFO, PlatformUtilsPlugin.PLUGIN_ID, "BEFORE " + executionEvent.getCommand().getId()));
				}
			}

		};
	}

	public IExecutionAfterInterceptor<T> initAfterInterceptor() {
		return new IExecutionAfterInterceptor<T>() {

			public void afterExecution(ExecutionEvent executionEvent, IExecutionSavedContext savedContext, T result) {
				if (executionEvent.getCommand() != null) {
					PlatformUtilsPlugin.getDefault().getLog().log(new Status(Status.INFO, PlatformUtilsPlugin.PLUGIN_ID, "AFTER " + executionEvent.getCommand().getId()));
				}
			}

		};
	}

	public IExecutionSavedContext initExecutionSavedContext() {
		return new ExecutionSavedContextImpl();
	}
}
