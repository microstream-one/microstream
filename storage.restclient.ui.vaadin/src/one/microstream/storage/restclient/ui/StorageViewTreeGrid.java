
package one.microstream.storage.restclient.ui;

import com.vaadin.flow.component.treegrid.TreeGrid;

import one.microstream.storage.restclient.types.StorageView;
import one.microstream.storage.restclient.types.StorageViewElement;


public class StorageViewTreeGrid extends TreeGrid<StorageViewElement>
{
	public StorageViewTreeGrid()
	{
		super();
	}
	
	public StorageViewTreeGrid(final StorageView storageView)
	{
		super();
		
		this.setDataProvider(StorageViewDataProvider.New(storageView), true);
	}
	
	public void setDataProvider(
		final StorageViewDataProvider<?> dataProvider
	)
	{
		this.setDataProvider(dataProvider, true);
	}
	
	public void setDataProvider(
		final StorageViewDataProvider<?> dataProvider,
		final boolean initDefaultColumns
	)
	{
		super.setDataProvider(dataProvider);
		
		if(initDefaultColumns)
		{
			this.addHierarchyColumn(StorageViewElement::name)
				.setHeader("Name")
				.setResizable(true);
			
			this.addColumn(StorageViewElement::value)
				.setHeader("Value")
				.setResizable(true);
		}
	}
}
