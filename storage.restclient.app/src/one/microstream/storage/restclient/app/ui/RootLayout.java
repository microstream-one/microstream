package one.microstream.storage.restclient.app.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import one.microstream.storage.restclient.app.SessionData;
import one.microstream.storage.restclient.app.resources.Resources;


@Push
@Theme(value = Lumo.class, variant = Lumo.DARK)
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@PageTitle("MicroStream Client App")
public class RootLayout extends VerticalLayout implements RouterLayout, BeforeEnterObserver
{
	private Component toolBar;
	private Label     headerLabel;
	
	public RootLayout()
	{
		super();

		this.add(this.createBanner(), new Hr());
		this.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
		this.setSizeFull();
	}
	
	private Component createBanner()
	{
		this.headerLabel = new Label();
		
		final Button cmdLogout = new Button("Logout", event -> {
			this.getUI().ifPresent(ui -> {
				ui.getSession().setAttribute(SessionData.class, null);
				ui.navigate(LoginView.class);
			});
		});
		cmdLogout.setIcon(VaadinIcon.EXIT.create());
		cmdLogout.addThemeVariants(ButtonVariant.LUMO_SMALL);
		
		final HorizontalLayout toolBar = new HorizontalLayout(cmdLogout);
		toolBar.setJustifyContentMode(JustifyContentMode.END);
		this.toolBar = toolBar;
		
		final HorizontalLayout banner = new HorizontalLayout(
			new Image(Resources.streamResource("Logo", "images/logo.png"), "Logo"),
			this.headerLabel,
			UIUtils.compact(toolBar));
		banner.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		banner.setFlexGrow(1, toolBar);
		
		return UIUtils.compact(banner);
	}
	
	@Override
	public void beforeEnter(
		final BeforeEnterEvent event
	)
	{
		final SessionData sessionData = event.getUI().getSession().getAttribute(SessionData.class);
		this.headerLabel.setText(
			sessionData != null
				? sessionData.baseUrl()
				: ""
		);
		this.toolBar.setVisible(
			   sessionData != null
			&& !event.getNavigationTarget().equals(LoginView.class)
		);
	}
}
