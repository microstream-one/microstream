
package one.microstream.storage.restclient.app.ui;

/*-
 * #%L
 * microstream-storage-restclient-app
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.storage.restclient.app.ui.UIUtils.imagePath;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import one.microstream.storage.restclient.app.types.SessionData;


@Route(value = "instance", layout = RootLayout.class)
public class InstanceView extends VerticalLayout implements HasDynamicTitle
{
	public InstanceView()
	{
		super();

		final StorageViewComponent       storageViewComponent       = new StorageViewComponent      ();
		final StorageStatisticsComponent storageStatisticsComponent = new StorageStatisticsComponent();

		final Tab storageViewTab       = new Tab(this.createTabComponent(
			imagePath("data.svg"),
			this.getTranslation("DATA")
		));
		storageViewTab.setId(ElementIds.TAB_DATA);
		final Tab storageStatisticsTab = new Tab(this.createTabComponent(
			imagePath("statistics.svg"),
			this.getTranslation("STATISTICS")
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


	private HorizontalLayout createTabComponent(final String image, final String text)
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
		final SessionData sessionData = this.getSessionData();
		return sessionData != null
			? sessionData.baseUrl() + " - " + RootLayout.PAGE_TITLE
			: RootLayout.PAGE_TITLE
		;
	}

	private SessionData getSessionData()
	{
		final UI ui = this.getUI().orElse(null);
		return ui != null
			? ui.getSession().getAttribute(SessionData.class)
			: null
		;
	}

}
