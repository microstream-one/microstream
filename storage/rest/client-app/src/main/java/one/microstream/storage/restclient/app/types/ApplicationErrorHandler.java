package one.microstream.storage.restclient.app.types;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.DefaultErrorHandler;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;

import one.microstream.storage.restclient.app.ui.InternalErrorView;


public class ApplicationErrorHandler implements ErrorHandler
{
	public static final String THROWABLE_ATTRIBUTE = ApplicationErrorHandler.class.getName() + "#THROWABLE";
	
	public static void handle(Throwable throwable)
	{
		LoggerFactory.getLogger(ApplicationErrorHandler.class)
			.error(throwable.getMessage(), throwable);
		
		VaadinSession.getCurrent().setAttribute(THROWABLE_ATTRIBUTE, throwable);
		UI.getCurrent().navigate(InternalErrorView.class);
	}
	
	
	public ApplicationErrorHandler()
	{
		super();
	}
	
	@Override
	public void error(ErrorEvent event)
	{
		handle(DefaultErrorHandler.findRelevantThrowable(event.getThrowable()));
	}
	
}
