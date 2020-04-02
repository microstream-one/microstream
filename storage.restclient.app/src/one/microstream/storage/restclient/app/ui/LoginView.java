
package one.microstream.storage.restclient.app.ui;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;

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
		
		final ComboBox<String> urlChooser = new ComboBox<>();
		urlChooser.setMinWidth("50ch");
		urlChooser.setDataProvider(DataProvider.ofCollection(this.urls()));
		urlChooser.setAllowCustomValue(true);
		urlChooser.addCustomValueSetListener(event -> urlChooser.setValue(event.getDetail()));
		
		final Button cmdLogin = new Button("Connect", 
			event -> {
				String url = urlChooser.getValue();
				if(url != null && (url = url.trim()).length() > 0)
				{
					this.tryLogin(url);
				}
				// re-enable button because disableOnClick=true
				event.getSource().setEnabled(true);
			}
		);
		cmdLogin.setIcon(new Image("images/login.svg", ""));
		cmdLogin.setDisableOnClick(true);
		
		final HorizontalLayout loginLayout = new HorizontalLayout(new Label("URL:"), urlChooser, cmdLogin);
		loginLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		
		VerticalLayout loginFrame = new VerticalLayout(
			new H3("Connect to instance"),
			loginLayout
		);
		loginFrame.setMargin(true);
		loginFrame.addClassName("box");
		loginFrame.setSizeUndefined();
		
		this.setHorizontalComponentAlignment(Alignment.CENTER, loginFrame);
		this.add(loginFrame);
		this.addClassName("header");
		this.setSizeFull();
	}
	
	private void tryLogin(
		final String baseUrl
	)
	{
		try(final StorageRestClientJersey client = StorageRestClientJersey.New(baseUrl))
		{
			client.requestRoot();
			
			updateUrlCookie(baseUrl);
			
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
				header.addClassName("error");
				
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
	
	private Set<String> urls()
	{
		final Set<String> urlSelection = new LinkedHashSet<>();
		urlSelection.add("http://localhost:4567/microstream/");
		
		final HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
		final Cookie[]           cookies = request.getCookies();
		if(cookies != null)
		{
			final Cookie urlCookie = Arrays.stream(cookies)
				.filter(c -> c.getName().equals("URL"))
				.findAny()
				.orElse(null);
			if(urlCookie != null)
			{
				final String urls = new String(
					Base64.getUrlDecoder().decode(urlCookie.getValue()), 
					StandardCharsets.UTF_8
				);
				for(final String url : urls.split("\n"))
				{
					urlSelection.add(url);
				}
			}
		}
		return urlSelection;
	}
	
	private void updateUrlCookie(
		final String url
	)
	{
		final Set<String> urls = this.urls();
		if(urls.add(url))
		{
			final String cookieData = urls.stream().collect(Collectors.joining("\n"));
			
			final HttpServletResponse response = (HttpServletResponse) VaadinResponse.getCurrent();
			final Cookie cookie = new Cookie(
				"URL", 
				new String(
					Base64.getUrlEncoder().encode(
						cookieData.getBytes(StandardCharsets.UTF_8)
					), 
					StandardCharsets.UTF_8
				)
			);
			response.addCookie(cookie);			
		}
	}
	
}
