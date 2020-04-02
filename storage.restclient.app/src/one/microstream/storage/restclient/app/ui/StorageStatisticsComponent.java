package one.microstream.storage.restclient.app.ui;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import one.microstream.storage.restadapter.ViewerChannelStatistics;
import one.microstream.storage.restadapter.ViewerFileStatistics;
import one.microstream.storage.restadapter.ViewerStorageFileStatistics;
import one.microstream.storage.restadapter.ViewerStorageFileStatisticsItem;
import one.microstream.storage.restclient.app.SessionData;
import one.microstream.storage.restclient.types.StorageRestClientJersey;


public class StorageStatisticsComponent extends TreeGrid<StorageStatisticsComponent.Item>
{
	public StorageStatisticsComponent()
	{
		super();
		
		this.setColumnReorderingAllowed(false);
		this.addHierarchyColumn(i -> i.name)
			.setHeader("Name")
			.setResizable(true)
			.setFrozen(true);
		this.addColumn(i -> i.value)
			.setHeader("Value")
			.setResizable(true);
		this.setSizeFull();
		this.addAttachListener(event -> {
			final SessionData sessionData = event.getUI().getSession().getAttribute(SessionData.class);
			final StorageRestClientJersey client = StorageRestClientJersey.New(sessionData.baseUrl());
			final ViewerStorageFileStatistics statistics = client.requestFileStatistics();
			this.setDataProvider(new StatisticsDataProvider(createItems(statistics)));
		});
	}
	
	private static List<Item> createItems(
		final ViewerStorageFileStatistics statistics
	)
	{		
		final List<Item> items = new ArrayList<>();
		
		items.add(new Item(
			"Creation time", 
			DateFormat.getDateTimeInstance().format(statistics.getCreationTime())
		));
		createCommonItems(statistics, items::add);
		
		final Item channelsItem = new Item("Channels", "");
		items.add(channelsItem);
		
		statistics.getChannelStatistics().values()
			.stream()
			.sorted((s1, s2) -> Integer.compare(s1.getChannelIndex(), s2.getChannelIndex()))
			.map(StorageStatisticsComponent::createChannelItem)
			.forEach(channelsItem::add);
		
		return items;
	}
	
	private static Item createChannelItem(
		final ViewerChannelStatistics statistics
	)
	{
		final Item channelItem = new Item(
			"Channel " + statistics.getChannelIndex(),
			""
		);
		
		createCommonItems(statistics, channelItem::add);
		
		final Item filesItem = new Item("Files", "");
		channelItem.add(filesItem);
		
		statistics.getFiles().forEach(
			fileStatistics -> filesItem.add(createFileItem(fileStatistics))
		);
		
		return channelItem;
	}
	
	private static Item createFileItem(
		final ViewerFileStatistics statistics
	)
	{
		final Item fileItem = new Item(
			"File " + statistics.getFileNumber(),
			statistics.getFile()
		);
		
		createDataItems(statistics, fileItem::add);
		
		return fileItem;
	}
	
	private static void createCommonItems(
		final ViewerStorageFileStatisticsItem statistics,
		final Consumer<Item> consumer
	)
	{
		consumer.accept(new Item(
			"File count", 
			Long.toString(statistics.getFileCount())
		));
		createDataItems(statistics, consumer);
	}
	
	private static void createDataItems(
		final ViewerStorageFileStatisticsItem statistics,
		final Consumer<Item> consumer
	)
	{
		consumer.accept(new Item(
			"Live data size", 
			humanReadableByteSize(statistics.getLiveDataLength())
		));
		consumer.accept(new Item(
			"Total data size", 
			humanReadableByteSize(statistics.getTotalDataLength())
		));
	}
	
	static String humanReadableByteSize(
		final long byteSize
	)
	{
		final NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setMinimumFractionDigits(0);
		
		double d = 0;
		if(byteSize < 1024)
		{
			return numberFormat.format(byteSize).concat(" Bytes");
		}
		else
		{
			d = byteSize / 1024d;
			if(d < 1024d)
			{
				return numberFormat.format(d).concat(" KB");
			}
			else
			{
				d /= 1024d;
				if(d < 1024d)
				{
					return numberFormat.format(d).concat(" MB");
				}
				else
				{
					d /= 1024d;
					return numberFormat.format(d).concat(" GB");
				}
			}
		}
	}
	
	static class Item
	{
		final String name;
		final String value;
		List<Item>   children;
		
		Item(
			final String name, 
			final String value
		)
		{
			this.name  = name;
			this.value = value;
		}
		
		void add(
			final Item item
		)
		{
			if(this.children == null)
			{
				this.children = new ArrayList<>();
			}
			this.children.add(item);
		}
	}
	
	
	static class StatisticsDataProvider 
		extends AbstractBackEndHierarchicalDataProvider<Item,Void>
	{
		private final List<Item> roots;
		
		StatisticsDataProvider(
			final List<Item> roots
		)
		{
			super();
			this.roots = roots;
		}

		@Override
		public boolean hasChildren(
			final Item item
		)
		{
			return item.children != null && item.children.size() > 0;
		}

		@Override
		public int getChildCount(HierarchicalQuery<Item, Void> query)
		{
			final Item parent = query.getParent();
			return parent == null
				? this.roots.size()
				: parent.children != null
					? parent.children.size()
					: 0;
		}

		@Override
		protected Stream<Item> fetchChildrenFromBackEnd(HierarchicalQuery<Item, Void> query)
		{
			final Item parent = query.getParent();
			Stream<Item> stream = 
				(parent == null 
					? this.roots
					: parent.children
				)
				.stream()
			;
			final Comparator<Item> comparator = query.getInMemorySorting();
			if(comparator != null)
			{
				stream = stream.sorted(comparator);
			}
			return stream
				.skip(query.getOffset())
				.limit(query.getLimit());
		}
		
	}
	
}
