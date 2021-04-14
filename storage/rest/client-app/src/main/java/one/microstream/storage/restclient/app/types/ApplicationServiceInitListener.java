
package one.microstream.storage.restclient.app.types;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

import one.microstream.storage.restclient.app.ui.ConnectView;


public class ApplicationServiceInitListener implements VaadinServiceInitListener
{
	public ApplicationServiceInitListener()
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
				if(!ConnectView.class.equals(enterEvent.getNavigationTarget()) &&
					enterEvent.getUI().getSession().getAttribute(SessionData.class) == null)
				{
					enterEvent.rerouteTo(ConnectView.class);
				}
			}
		));
	}
	
}
