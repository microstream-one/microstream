package one.microstream.storage.restclient.app.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import one.microstream.storage.restclient.app.ApplicationErrorHandler;
import one.microstream.storage.restclient.app.SessionData;

@Route(value = "error", layout = RootLayout.class)
@ParentLayout(RootLayout.class)
@PageTitle("Error - " + RootLayout.PAGE_TITLE)
public class InternalErrorView extends VerticalLayout 
	implements HasErrorParameter<Exception>, BeforeEnterObserver
{
	public static String restServiceHint(String baseUrl)
	{
		return "Please ensure that a started REST service is available at " + baseUrl;
	}
	
	
	public InternalErrorView()
	{
		super();

		this.setSizeFull();
	}
	
	@Override
	public int setErrorParameter(
		final BeforeEnterEvent event, 
		final ErrorParameter<Exception> parameter
	)
	{
		final VaadinSession session = event.getUI().getSession();
		session.setAttribute(
			ApplicationErrorHandler.THROWABLE_ATTRIBUTE,
			parameter.getCaughtException()
		);
		
		return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}

	@Override
	public void beforeEnter(
		final BeforeEnterEvent event
	)
	{					
		final H3 header = new H3("An internal error occured.");
		header.addClassName("error");
		this.add(header);

		final VaadinSession session = event.getUI().getSession();
		final SessionData sessionData = session.getAttribute(SessionData.class);
		if(sessionData != null)
		{
			this.add(new Label(restServiceHint(sessionData.baseUrl()))); 
		}
		
		final Throwable t = (Throwable)session.getAttribute(
			ApplicationErrorHandler.THROWABLE_ATTRIBUTE
		);
		if(t != null)
		{
			this.add(new Hr());
			
			final StringWriter stringWriter = new StringWriter();
			try(final PrintWriter writer = new PrintWriter(stringWriter))
			{
				t.printStackTrace(writer);
			}
			
			final TextArea stackTrace = new TextArea();
			stackTrace.setValue(stringWriter.toString());
			stackTrace.setReadOnly(true);
			stackTrace.setWidth("100%");
			
			final Details details = new Details("Details", stackTrace);
			details.setOpened(false);
			this.add(details);
			this.setHorizontalComponentAlignment(Alignment.STRETCH, details);
			this.setFlexGrow(1, details);
		}
		
	}
	
}
