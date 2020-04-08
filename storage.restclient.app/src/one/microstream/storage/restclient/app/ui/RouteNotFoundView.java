package one.microstream.storage.restclient.app.ui;

import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteNotFoundError;

@Route(value = "404", layout = RootLayout.class)
@PageTitle("404 - " + RootLayout.PAGE_TITLE)
public class RouteNotFoundView extends RouteNotFoundError
{
	public RouteNotFoundView()
	{
		super();
	}
	
	@Override
	public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter)
	{
		getElement().appendChild(new Span("404 - not found").getElement());
		
        return HttpServletResponse.SC_NOT_FOUND;
	}
}
