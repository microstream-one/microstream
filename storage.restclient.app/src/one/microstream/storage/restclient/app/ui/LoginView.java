
package one.microstream.storage.restclient.app.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;

import one.microstream.storage.restclient.app.SessionData;
import one.microstream.storage.restclient.types.StorageRestClientJersey;


@Route(value = "", layout = RootLayout.class)
@ParentLayout(RootLayout.class)
@PageTitle("Login - " + RootLayout.PAGE_TITLE)
public class LoginView extends VerticalLayout
{
	public LoginView()
	{
		super();
		
		final TextField txtUrl = new TextField();
		txtUrl.setMinWidth("50ch");
		txtUrl.setValue("http://localhost:4567/microstream/");
		
		final Button cmdLogin = new Button("Login", 
			event -> this.tryLogin(txtUrl.getValue())
		);
		
		final HorizontalLayout loginLayout = new HorizontalLayout(new Label("URL:"), txtUrl, cmdLogin);
		loginLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		
		final VerticalLayout centerLayout = new VerticalLayout(loginLayout);
		centerLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		centerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		centerLayout.setSizeFull();
		this.add(centerLayout);
		this.setSizeFull();
	}
	
	private void tryLogin(final String baseUrl)
	{
		try(StorageRestClientJersey client = StorageRestClientJersey.New(baseUrl))
		{
			client.requestRoot();
			
			final SessionData sessionData = new SessionData(baseUrl);
			this.getUI().ifPresent(ui -> {
				ui.getSession().setAttribute(SessionData.class, sessionData);
				ui.navigate(InstanceView.class);
			});
		}
		catch(Exception e)
		{
			this.getUI().ifPresent(ui -> {

				final Notification notification = new Notification();
								
				final H3 header = new H3("Error connecting to instance.");
				header.addClassName(UIUtils.addErrorStyle(ui));
				
				final Button close = new Button("OK", event -> notification.close());
				
				final VerticalLayout content = new VerticalLayout(
					header,
					new Hr(),
					new Label(InternalErrorView.restServiceHint(baseUrl)),
					close 
				);
				content.setHorizontalComponentAlignment(Alignment.END, close);
				
				notification.add(content);
				notification.setDuration(0);
				notification.setPosition(Position.MIDDLE);
				notification.open();
			});
		}
	}
	
}
