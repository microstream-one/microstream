
package one.microstream.storage.restclient.ui;

import java.util.stream.Stream;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import one.microstream.storage.restclient.types.StorageView;
import one.microstream.storage.restclient.types.StorageViewElement;


public interface StorageViewDataProvider<F> extends HierarchicalDataProvider<StorageViewElement, F>
{
	public static <F> StorageViewDataProvider<F> New(final StorageView storageView)
	{
		return new Default<>(storageView);
	}
	
	public static class Default<F>
		extends AbstractBackEndHierarchicalDataProvider<StorageViewElement, F>
		implements StorageViewDataProvider<F>
	{
		private final StorageView storageView;
		
		Default(final StorageView storageView)
		{
			super();
			
			this.storageView = storageView;
		}
		
		@Override
		public boolean hasChildren(
			final StorageViewElement item
		)
		{
			return item.hasMembers();
		}
		
		@Override
		public int getChildCount(
			final HierarchicalQuery<StorageViewElement, F> query
		)
		{
			final StorageViewElement parent = query.getParent();
			return parent == null
				? 1
				: parent.hasMembers()
					? parent.members(false).size()
					: 0;
		}
		
		@Override
		protected Stream<StorageViewElement> fetchChildrenFromBackEnd(
			final HierarchicalQuery<StorageViewElement, F> query
		)
		{
			final StorageViewElement parent = query.getParent();
			return parent == null
				? Stream.of(this.storageView.root())
				: query.getParent().members(false).stream();
		}
		
	}
	
}
