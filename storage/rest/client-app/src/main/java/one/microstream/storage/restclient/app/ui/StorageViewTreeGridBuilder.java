
package one.microstream.storage.restclient.app.ui;

import java.util.Comparator;

import com.vaadin.flow.component.Component;
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
	
	
	public static StorageViewTreeGridBuilder New(final Component context)
	{
		return new Default(context);
	}
	
	
	public static class Default implements StorageViewTreeGridBuilder
	{
		private final Component            context;
		private StorageViewDataProvider<?> dataProvider;
		private boolean                    defaultColumns = true;
		
		Default(final Component context)
		{
			super();
			this.context = context;
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
				.setHeader(this.context.getTranslation("NAME"))
				.setResizable(true)
				.setFrozen(true)
				.setComparator(new NameComparator());
		
			treeGrid.addColumn(Default::elementValue)
				.setHeader(this.context.getTranslation("VALUE"))
				.setResizable(true);
					
			treeGrid.addColumn(StorageViewElement::simpleTypeName)
				.setHeader(this.context.getTranslation("TYPE"))
				.setResizable(true);
		
			treeGrid.addColumn(Default::elementObjectId)
				.setHeader(this.context.getTranslation("OBJECT_ID"))
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
		
		
		private static class NameComparator implements Comparator<StorageViewElement>
		{
			NameComparator()
			{
				super();
			}
			
			@Override
			public int compare(
				final StorageViewElement e1, 
				final StorageViewElement e2
			)
			{
				final String name1 = e1.name();
				final String name2 = e2.name();
				
				final Integer rangeIndex1 = getRangeIndex(name1);
				final Integer rangeIndex2;
				if(rangeIndex1 != null && (rangeIndex2 = getRangeIndex(name2)) != null)
				{
					return rangeIndex1.compareTo(rangeIndex2);
				}
				
				return name1.compareTo(name2);
			}
			
			private static Integer getRangeIndex(String name)
			{				
				if(name.length() > 2 
					&& name.charAt(0) == '[' 
					&& name.charAt(name.length() - 1) == ']')
				{
					int dots;
					if(name.length() > 4 && (dots = name.indexOf("..")) != -1)
					{
						// [n..m]
						try
						{
							return Integer.parseInt(name.substring(1, dots));
						}
						catch(NumberFormatException e)
						{
							// swallow
						}
					}
					else
					{
						// [n]
						try
						{
							return Integer.parseInt(name.substring(1, name.length() - 1));
						}
						catch(NumberFormatException e)
						{
							// swallow
						}
					}
				}
				
				return null;
			}
			
		}
		
	}
	
}
