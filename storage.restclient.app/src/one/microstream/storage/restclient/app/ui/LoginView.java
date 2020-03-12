
package one.microstream.storage.restclient.app.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;

import one.microstream.storage.restclient.app.SessionData;


@Route(value = "", layout = RootLayout.class)
@ParentLayout(RootLayout.class)
public class LoginView extends VerticalLayout
{
	public LoginView()
	{
		super();
		
		final TextField txtUrl = new TextField();
		txtUrl.setMinWidth("30ch");
		txtUrl.setValue("http://localhost:4567/microstream/");
		final Button cmdLogin = new Button("Login", event -> {
			final String baseUrl = txtUrl.getValue();
			final SessionData sessionData = new SessionData(baseUrl);
			this.getUI().ifPresent(ui -> {
				ui.getSession().setAttribute(SessionData.class, sessionData);
				ui.navigate(InstanceView.class);
			});
		});
		
		final HorizontalLayout loginLayout = new HorizontalLayout(new Label("URL:"), txtUrl, cmdLogin);
		loginLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		
		final VerticalLayout centerLayout = new VerticalLayout(loginLayout);
		centerLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		centerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		centerLayout.setSizeFull();
		this.add(centerLayout);
		this.setSizeFull();
	}
}
