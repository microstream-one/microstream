
package one.microstream.storage.restclient.app.ui;

import static one.microstream.storage.restclient.app.ui.UIUtils.imagePath;

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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;

import one.microstream.storage.restclient.app.types.SessionData;
import one.microstream.storage.restclient.jersey.types.StorageRestClientJersey;


@Route(value = "", layout = RootLayout.class)
public class ConnectView extends VerticalLayout implements HasDynamicTitle
{	
	public ConnectView()
	{
		super();
		
		final ComboBox<String> urlChooser = new ComboBox<>();
		urlChooser.setId(ElementIds.COMBO_URL);
		urlChooser.setMinWidth("50ch");
		urlChooser.setDataProvider(DataProvider.ofCollection(this.urls()));
		urlChooser.setAllowCustomValue(true);
		urlChooser.addCustomValueSetListener(event -> urlChooser.setValue(event.getDetail()));
		
		final Button cmdConnect = new Button(this.getTranslation("CONNECT"), 
			event -> {
				String url = urlChooser.getValue();
				if(url != null && (url = url.trim()).length() > 0)
				{
					this.tryConnect(url);
				}
				// re-enable button because disableOnClick=true
				event.getSource().setEnabled(true);
			}
		);
		cmdConnect.setId(ElementIds.BUTTON_CONNECT);
		cmdConnect.setIcon(new Image(imagePath("login.svg"), ""));
		cmdConnect.setDisableOnClick(true);
		
		final HorizontalLayout connectLayout = new HorizontalLayout(
			new Label(this.getTranslation("URL") + ":"), 
			urlChooser, 
			cmdConnect
		);
		connectLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		
		final VerticalLayout connectFrame = new VerticalLayout(
			new H3(this.getTranslation("CONNECT_HEADER")),
			connectLayout
		);
		connectFrame.setMargin(true);
		connectFrame.addClassName(ClassNames.BOX);
		connectFrame.setSizeUndefined();
		
		this.setHorizontalComponentAlignment(Alignment.CENTER, connectFrame);
		this.add(connectFrame);
		this.addClassName(ClassNames.BACKGROUND_THEME);
		this.setSizeFull();
	}
	
	@Override
	public String getPageTitle()
	{
		return this.getTranslation("CONNECT") + " - " + RootLayout.PAGE_TITLE;
	}
	
	private void tryConnect(
		final String baseUrl
	)
	{
		try(final StorageRestClientJersey client = StorageRestClientJersey.New(baseUrl))
		{
			client.requestRoot();
			
			this.updateUrlCookie(baseUrl);
			
			final SessionData sessionData = new SessionData(baseUrl);
			this.getUI().ifPresent(ui -> {
				ui.getSession().setAttribute(SessionData.class, sessionData);
				ui.navigate(InstanceView.class);
			});
		}
		catch(final Exception e)
		{
			this.getUI().ifPresent(ui -> {

				final Notification notification = new Notification();
				
				final H3 header = new H3(this.getTranslation("CONNECT_ERROR"));
				header.addClassName(ClassNames.ERROR);
				
				final Button close = new Button(
					this.getTranslation("OK"), 
					event -> notification.close()
				);
				
				final VerticalLayout content = new VerticalLayout(
					header,
					new Hr(),
					new Label(this.getTranslation("INTERNAL_ERROR_HINT", baseUrl)),
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
