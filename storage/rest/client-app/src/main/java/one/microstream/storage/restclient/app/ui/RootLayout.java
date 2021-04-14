package one.microstream.storage.restclient.app.ui;

import static one.microstream.storage.restclient.app.ui.UIUtils.imagePath;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import one.microstream.storage.restclient.app.types.SessionData;


@Push
@Theme(value = Lumo.class, variant = Lumo.DARK)
@HtmlImport("styles/shared-styles.html")
public class RootLayout extends VerticalLayout 
	implements RouterLayout, BeforeEnterObserver, PageConfigurator
{
	public final static String PAGE_TITLE = "MicroStream Client";
	
	private Component toolBar;
	private Label     headerLabel;
	
	public RootLayout()
	{
		super();

		this.add(this.createHeader());
		this.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
		this.setMargin(false);
		this.setPadding(false);
		this.setSizeFull();
	}
	
	private Component createHeader()
	{
		this.headerLabel = new Label();
		
		final Button cmdDisconnect = new Button(this.getTranslation("DISCONNECT"), event -> {
			this.getUI().ifPresent(ui -> {
				ui.getSession().setAttribute(SessionData.class, null);
				ui.navigate(ConnectView.class);
			});
		});
		cmdDisconnect.setId(ElementIds.BUTTON_DISCONNECT);
		cmdDisconnect.setIcon(new Image(imagePath("logout.svg"), ""));
		cmdDisconnect.addThemeVariants(ButtonVariant.LUMO_SMALL);
		
		final HorizontalLayout toolBar = new HorizontalLayout(cmdDisconnect);
		toolBar.setJustifyContentMode(JustifyContentMode.END);
		this.toolBar = toolBar;
		
		final HorizontalLayout header = new HorizontalLayout(
			new Image(imagePath("logo.png"), "Logo"),
			this.headerLabel,
			UIUtils.compact(toolBar));
		header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		header.setFlexGrow(1, toolBar);
		
		header.addClassName(ClassNames.HEADER);
		
		return UIUtils.compact(header);
	}
	
	@Override
	public void beforeEnter(
		final BeforeEnterEvent event
	)
	{		
		final SessionData sessionData = event.getUI().getSession().getAttribute(SessionData.class);
		this.headerLabel.setText(
			sessionData != null
				? this.getTranslation("CLIENT") + " - " + sessionData.baseUrl()
				: this.getTranslation("CLIENT")
		);
		this.toolBar.setVisible(
			   sessionData != null
			&& !event.getNavigationTarget().equals(ConnectView.class)
		);
	}
	
	@Override
	public void configurePage(
		final InitialPageSettings settings
	)
	{
		settings.addLink   ("shortcut icon", imagePath("icon.ico")           );
		settings.addFavIcon("icon"         , imagePath("icon.png"), "256x256");
	}
}
