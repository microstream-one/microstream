
package one.microstream.storage.restclient.app;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

import one.microstream.storage.restclient.app.ui.LoginView;


public class AppServiceInitListener implements VaadinServiceInitListener
{
	public AppServiceInitListener()
	{
		super();
	}
	
	@Override
	public void serviceInit(
		final ServiceInitEvent serviceInitEvent
	)
	{		
		final VaadinService service = serviceInitEvent.getSource();
		
		service.addSessionInitListener(sessionInitEvent -> 
			sessionInitEvent.getSession().setErrorHandler(new ApplicationErrorHandler())
		);
		
		service.addUIInitListener(
			uiInitEvent -> uiInitEvent.getUI().addBeforeEnterListener(enterEvent -> {
				if(!LoginView.class.equals(enterEvent.getNavigationTarget()) &&
					enterEvent.getUI().getSession().getAttribute(SessionData.class) == null)
				{
					enterEvent.rerouteTo(LoginView.class);
				}
			}
		));
	}
	
}
