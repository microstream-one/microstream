
package one.microstream.storage.restclient.app.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout.Orientation;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;

import one.microstream.storage.restclient.app.SessionData;
import one.microstream.storage.restclient.types.StorageRestClientJersey;
import one.microstream.storage.restclient.types.StorageView;
import one.microstream.storage.restclient.types.StorageViewConfiguration;
import one.microstream.storage.restclient.types.StorageViewElement;
import one.microstream.storage.restclient.ui.StorageViewDataProvider;
import one.microstream.storage.restclient.ui.StorageViewElementField;
import one.microstream.storage.restclient.ui.StorageViewTreeGridBuilder;


@Route(value = "instance", layout = RootLayout.class)
@ParentLayout(RootLayout.class)
public class InstanceView extends VerticalLayout
{
	public InstanceView()
	{
		super();
		
		final TreeGrid<StorageViewElement> treeGrid     = StorageViewTreeGridBuilder.New().build();
		final StorageViewElementField elementField = new StorageViewElementField();
		
		final SplitLayout splitLayout = new SplitLayout(treeGrid, elementField);
		splitLayout.setOrientation(Orientation.VERTICAL);
		splitLayout.setSplitterPosition(65);
		splitLayout.setSizeFull();
		this.add(splitLayout);
		this.setSizeFull();
		
		treeGrid.addSelectionListener(event ->
			elementField.setValue(
				event.getFirstSelectedItem().orElse(null)
			)
		);
		
		this.addAttachListener(event -> {
			final SessionData sessionData = event.getUI().getSession().getAttribute(SessionData.class);
			final StorageView storageView = StorageView.New(
				StorageViewConfiguration.Default(),
				StorageRestClientJersey.New(sessionData.baseUrl())
			);
			treeGrid.setDataProvider(StorageViewDataProvider.New(storageView));
		});
	}
}
