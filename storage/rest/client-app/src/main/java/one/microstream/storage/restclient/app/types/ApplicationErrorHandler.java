package one.microstream.storage.restclient.app.types;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.DefaultErrorHandler;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;

import one.microstream.storage.restclient.app.ui.InternalErrorView;
import one.microstream.util.logging.Logging;


public class ApplicationErrorHandler implements ErrorHandler
{
	public static final String THROWABLE_ATTRIBUTE = ApplicationErrorHandler.class.getName() + "#THROWABLE";
	
	public static void handle(final Throwable throwable)
	{
		Logging.getLogger(ApplicationErrorHandler.class)
			.error(throwable.getMessage(), throwable);
		
		VaadinSession.getCurrent().setAttribute(THROWABLE_ATTRIBUTE, throwable);
		UI.getCurrent().navigate(InternalErrorView.class);
	}
	
	
	public ApplicationErrorHandler()
	{
		super();
	}
	
	@Override
	public void error(final ErrorEvent event)
	{
		handle(DefaultErrorHandler.findRelevantThrowable(event.getThrowable()));
	}
	
}
