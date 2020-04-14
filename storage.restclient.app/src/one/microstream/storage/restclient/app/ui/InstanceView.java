
package one.microstream.storage.restclient.app.ui;

import static one.microstream.storage.restclient.app.ui.UIUtils.imagePath;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import one.microstream.storage.restclient.app.SessionData;


@Route(value = "instance", layout = RootLayout.class)
public class InstanceView extends VerticalLayout implements HasDynamicTitle
{
	public InstanceView()
	{
		super();
		
		final StorageViewComponent       storageViewComponent       = new StorageViewComponent      ();
		final StorageStatisticsComponent storageStatisticsComponent = new StorageStatisticsComponent();
				
		final Tab storageViewTab       = new Tab(createTabComponent(
			imagePath("data.svg"),
			getTranslation("DATA")
		));
		storageViewTab.setId(ElementIds.TAB_DATA);
		final Tab storageStatisticsTab = new Tab(createTabComponent(
			imagePath("statistics.svg"), 
			getTranslation("STATISTICS")
		));
		storageStatisticsTab.setId(ElementIds.TAB_STATISTICS);
		
		final Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(storageViewTab      , storageViewComponent      );
		tabsToPages.put(storageStatisticsTab, storageStatisticsComponent);
		
		final Tabs tabs = new Tabs(
			storageViewTab, 
			storageStatisticsTab
		);
				
		final Div pages = new Div(
			storageViewComponent,
			storageStatisticsComponent
		);

		final Component[] visiblePage = {storageViewComponent};
		tabsToPages.values().forEach(c -> c.setVisible(c == visiblePage[0]));		
		tabs.addSelectedChangeListener(event -> {
			visiblePage[0].setVisible(false);
		    (visiblePage[0] = tabsToPages.get(tabs.getSelectedTab())).setVisible(true);
		});
				
		this.setPadding(false);
		this.setMargin(false);
		this.setSpacing(false);
		this.add(tabs,pages);
		this.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
		this.setFlexGrow(1.0, pages);
		this.setSizeFull();
	}
	
	
	private HorizontalLayout createTabComponent(String image, String text)
	{
		final HorizontalLayout layout = new HorizontalLayout(
			new Image(image, text), 
			new Span(text)
		);
		layout.setAlignItems(Alignment.CENTER);
		layout.setSpacing(true);
		layout.setPadding(false);
		layout.setMargin(false);
		return layout;
	}
	
	@Override
	public String getPageTitle()
	{
		SessionData sessionData = getUI().get().getSession().getAttribute(SessionData.class);
		return sessionData.baseUrl() + " - " + RootLayout.PAGE_TITLE;
	}
}
