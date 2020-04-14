package one.microstream.storage.restclient.app.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;

import one.microstream.storage.restclient.StorageView;
import one.microstream.storage.restclient.StorageViewConfiguration;
import one.microstream.storage.restclient.StorageViewElement;
import one.microstream.storage.restclient.app.SessionData;
import one.microstream.storage.restclient.jersey.StorageRestClientJersey;

public class StorageViewComponent extends SplitLayout
{
	public StorageViewComponent()
	{
		super();

		this.setOrientation(Orientation.VERTICAL);
		this.setSplitterPosition(65);
		this.setSizeFull();
		
		final TreeGrid<StorageViewElement> treeGrid = StorageViewTreeGridBuilder.New(this).build();
		treeGrid.setId(ElementIds.GRID_DATA);
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
					final TreeGrid<StorageViewElement> detailTreeGrid = StorageViewTreeGridBuilder.New(this).build();
					detailTreeGrid.setId(ElementIds.GRID_DETAIL);
					detailTreeGrid.setDataProvider(StorageViewDataProvider.New(element));
					detailTreeGrid.expand(element);
					detailTreeGrid.setSizeFull();
					secondaryDiv.add(detailTreeGrid);
				}
				else
				{
					final String   value    = element.value();
					final TextArea textArea = new TextArea();
					textArea.setId(ElementIds.TEXT_DETAIL);
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
}
