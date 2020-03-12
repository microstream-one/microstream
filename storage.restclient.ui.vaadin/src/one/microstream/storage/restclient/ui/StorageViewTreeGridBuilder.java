
package one.microstream.storage.restclient.ui;

import com.vaadin.flow.component.treegrid.TreeGrid;

import one.microstream.storage.restclient.types.StorageView;
import one.microstream.storage.restclient.types.StorageViewElement;
import one.microstream.storage.restclient.types.StorageViewObject;


public interface StorageViewTreeGridBuilder
{
	public StorageViewTreeGridBuilder dataProvider(StorageViewDataProvider<?> dataProvider);
	
	public default StorageViewTreeGridBuilder dataProvider(final StorageView storageView)
	{
		return this.dataProvider(StorageViewDataProvider.New(storageView));
	}
	
	public StorageViewTreeGridBuilder withDefaultColumns();
	
	public StorageViewTreeGridBuilder withoutDefaultColumns();
	
	public TreeGrid<StorageViewElement> build();
	
	
	public static StorageViewTreeGridBuilder New()
	{
		return new Default();
	}
	
	
	public static class Default implements StorageViewTreeGridBuilder
	{
		private StorageViewDataProvider<?> dataProvider;
		private boolean defaultColumns = true;
		
		Default()
		{
			super();
		}
		
		@Override
		public StorageViewTreeGridBuilder dataProvider(
			final StorageViewDataProvider<?> dataProvider
		)
		{
			this.dataProvider = dataProvider;
			return this;
		}
		
		@Override
		public StorageViewTreeGridBuilder withDefaultColumns()
		{
			this.defaultColumns = true;
			return this;
		}
		
		@Override
		public StorageViewTreeGridBuilder withoutDefaultColumns()
		{
			this.defaultColumns = false;
			return this;
		}
		
		@Override
		public TreeGrid<StorageViewElement> build()
		{
			final TreeGrid<StorageViewElement> treeGrid = new TreeGrid<>();
			treeGrid.setColumnReorderingAllowed(true);
			
			if(this.dataProvider != null)
			{
				treeGrid.setDataProvider(this.dataProvider);
			}
			if(this.defaultColumns)
			{
				this.initDefaultColumns(treeGrid);
			}
			
			return treeGrid;
		}
		
		private void initDefaultColumns(final TreeGrid<StorageViewElement> treeGrid)
		{
			treeGrid.addHierarchyColumn(StorageViewElement::name)
				.setHeader("Name")
				.setResizable(true)
				.setFrozen(true);
		
			treeGrid.addColumn(Default::elementValue)
				.setHeader("Value")
				.setResizable(true);
					
			treeGrid.addColumn(StorageViewElement::simpleTypeName)
				.setHeader("Type")
				.setResizable(true);
		
			treeGrid.addColumn(Default::elementObjectId)
				.setHeader("ObjectId")
				.setResizable(true);
		}

		private static Object elementValue(
			final StorageViewElement element
		)
		{
			String value = element.value();
			return value != null && value.length() > 0
				? value
				: (value = element.simpleTypeName()) != null && value.length() > 0
					? "(" + value + ")"
					: ""
			;
		}

		private static Object elementObjectId(
			final StorageViewElement element
		)
		{
			return element instanceof StorageViewObject
				? Long.toString(((StorageViewObject)element).objectId())
				: "";
		}
		
	}
	
}
