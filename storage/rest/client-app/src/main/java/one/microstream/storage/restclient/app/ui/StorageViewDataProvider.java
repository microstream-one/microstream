
package one.microstream.storage.restclient.app.ui;

import static one.microstream.X.notNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import one.microstream.storage.restclient.app.types.ApplicationErrorHandler;
import one.microstream.storage.restclient.types.StorageView;
import one.microstream.storage.restclient.types.StorageViewElement;


public interface StorageViewDataProvider<F> extends HierarchicalDataProvider<StorageViewElement, F>
{
	public static <F> StorageViewDataProvider<F> New(final StorageView storageView)
	{
		notNull(storageView);
		return new Default<>(() -> storageView.root());
	}

	public static <F> StorageViewDataProvider<F> New(final StorageViewElement root)
	{
		notNull(root);
		return new Default<>(() -> root);
	}


	public static class Default<F>
		extends AbstractBackEndHierarchicalDataProvider<StorageViewElement, F>
		implements StorageViewDataProvider<F>
	{
		private final Supplier<StorageViewElement> rootSupplier;
		private final HashSet<StorageViewElement>  dirtyElements = new HashSet<>();

		Default(final Supplier<StorageViewElement> rootSupplier)
		{
			super();
			this.rootSupplier = rootSupplier;
		}

		@Override
		public boolean hasChildren(
			final StorageViewElement item
		)
		{
			try
			{
				return item.hasMembers();
			}
			catch(final Exception e)
			{
				ApplicationErrorHandler.handle(e);
				return false;
			}
		}

		@Override
		public int getChildCount(
			final HierarchicalQuery<StorageViewElement, F> query
		)
		{
			try
			{
				final StorageViewElement parent = query.getParent();
				return parent == null
					? 1 // root
					: parent.hasMembers()
						? parent.members(this.dirtyElements.remove(parent)).size()
						: 0;
			}
			catch(final Exception e)
			{
				ApplicationErrorHandler.handle(e);
				return 0;
			}
		}

		@Override
		protected Stream<StorageViewElement> fetchChildrenFromBackEnd(
			final HierarchicalQuery<StorageViewElement, F> query
		)
		{
			try
			{
				final StorageViewElement parent = query.getParent();
				Stream<StorageViewElement> stream = parent == null
					? Stream.of(this.rootSupplier.get())
					: parent.members(this.dirtyElements.remove(parent)).stream();
				final Comparator<StorageViewElement> comparator = query.getInMemorySorting();
				if(comparator != null)
				{
					stream = stream.sorted(comparator);
				}
				return stream
					.skip(query.getOffset())
					.limit(query.getLimit());
			}
			catch(final Exception e)
			{
				ApplicationErrorHandler.handle(e);
				return Stream.empty();
			}
		}

		@Override
		public void refreshItem(
			final StorageViewElement item,
			final boolean refreshChildren
		)
		{
			try
			{
				this.dirtyElements.add(item);

				super.refreshItem(item, refreshChildren);
			}
			catch(final Exception e)
			{
				ApplicationErrorHandler.handle(e);
			}
		}

	}

}
