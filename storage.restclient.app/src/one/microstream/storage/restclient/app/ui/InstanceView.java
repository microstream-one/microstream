
package one.microstream.storage.restclient.app.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;

import one.microstream.storage.restclient.app.SessionData;
import one.microstream.storage.restclient.types.StorageRestClientJersey;
import one.microstream.storage.restclient.types.StorageView;
import one.microstream.storage.restclient.types.StorageViewConfiguration;
import one.microstream.storage.restclient.types.StorageViewElement;
import one.microstream.storage.restclient.ui.StorageViewDataProvider;
import one.microstream.storage.restclient.ui.StorageViewTreeGridBuilder;


@Route(value = "instance", layout = RootLayout.class)
@ParentLayout(RootLayout.class)
public class InstanceView extends VerticalLayout
{
	private final TreeGrid<StorageViewElement> treeGrid;
	
	
	public InstanceView()
	{
		super();
		
		this.treeGrid = StorageViewTreeGridBuilder.New().build();
		this.treeGrid.setSizeFull();
		this.add(this.treeGrid);
		
		this.addAttachListener(event ->
			this.update(
				event.getUI().getSession().getAttribute(SessionData.class)
			)
		);
		
		this.setSizeFull();
	}
	
	private void update(final SessionData sessionData)
	{
		final StorageView storageView = StorageView.New(
			StorageViewConfiguration.Default(),
			StorageRestClientJersey.New(sessionData.baseUrl())
		);
		this.treeGrid.setDataProvider(StorageViewDataProvider.New(storageView));
	}
}
