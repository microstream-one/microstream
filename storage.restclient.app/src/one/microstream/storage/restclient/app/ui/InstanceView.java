
package one.microstream.storage.restclient.app.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;

import one.microstream.storage.restclient.app.SessionData;
import one.microstream.storage.restclient.types.StorageRestClientJersey;
import one.microstream.storage.restclient.types.StorageView;
import one.microstream.storage.restclient.types.StorageViewConfiguration;
import one.microstream.storage.restclient.types.StorageViewElement;


@Route(value = "instance", layout = RootLayout.class)
@ParentLayout(RootLayout.class)
public class InstanceView extends SplitLayout implements HasDynamicTitle
{
	public InstanceView()
	{
		super();

		this.setOrientation(Orientation.VERTICAL);
		this.setSplitterPosition(65);
		this.setSizeFull();
		
		final TreeGrid<StorageViewElement> treeGrid = StorageViewTreeGridBuilder.New().build();
		this.addToPrimary(treeGrid);	
		
		final Div secondaryDiv = new Div();
		this.addToSecondary(secondaryDiv);
		
		treeGrid.addSelectionListener(event -> {
			final StorageViewElement element = event.getFirstSelectedItem().orElse(null);
			secondaryDiv.removeAll();
			if(element != null)
			{
				if(element.hasMembers())
				{
					final TreeGrid<StorageViewElement> detailTreeGrid = StorageViewTreeGridBuilder.New().build();
					detailTreeGrid.setDataProvider(StorageViewDataProvider.New(element));
					detailTreeGrid.expand(element);
					detailTreeGrid.setSizeFull();
					secondaryDiv.add(detailTreeGrid);
				}
				else
				{
					final String   value    = element.value();
					final TextArea textArea = new TextArea();
					textArea.setValue(value != null ? value : "");
					textArea.setReadOnly(true);
					textArea.setWidth("100%");
					secondaryDiv.add(textArea);
				}
			}
		});
		
		this.addAttachListener(event -> {
			final SessionData sessionData = event.getUI().getSession().getAttribute(SessionData.class);
			final StorageView storageView = StorageView.New(
				StorageViewConfiguration.Default(),
				StorageRestClientJersey.New(sessionData.baseUrl())
			);
			treeGrid.setDataProvider(StorageViewDataProvider.New(storageView));
		});
	}
	
	@Override
	public String getPageTitle()
	{
		SessionData sessionData = getUI().get().getSession().getAttribute(SessionData.class);
		return sessionData.baseUrl() + " - " + RootLayout.PAGE_TITLE;
	}
}
