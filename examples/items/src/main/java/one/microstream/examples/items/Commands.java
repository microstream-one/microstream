package one.microstream.examples.items;

import java.util.List;
import java.util.stream.Collectors;

import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "")
public class Commands
{
	public static CommandLine createCommandLine(final EmbeddedStorageManager storageManager)
	{
		final CommandLine cli = new CommandLine(new Commands());
		cli.addSubcommand(new AddCommand(storageManager));
		cli.addSubcommand(new RemoveCommand(storageManager));
		cli.addSubcommand(new SearchCommand(storageManager));
		cli.addSubcommand(new ListCommand(storageManager));
		cli.addSubcommand(new QuitCommand(storageManager));
		return cli;
	}
	
	static abstract class Abstract implements Runnable
	{
		final EmbeddedStorageManager storageManager;

		Abstract(final EmbeddedStorageManager storageManager)
		{
			this.storageManager = storageManager;
		}

		DataRoot data()
		{
			return (DataRoot)this.storageManager.root();
		}

		void print(final List<Item> items)
		{
			if(items.isEmpty())
			{
				System.out.println("No items found");
			}
			else
			{
				System.out.println("Found " + items.size() + " item(s):");
				for(int i = 0, c = items.size(); i < c; i++)
				{
					System.out.println(i + " " + items.get(i));
				}
			}
		}
	}
	
	@Command(
		name = "add",
		aliases = {"a"},
		description = "Adds an item",
		mixinStandardHelpOptions = true
	)
	static class AddCommand extends Abstract
	{
		@Parameters(
			index = "0",
			description = "the new item name"
		)
		String item;
		
		AddCommand(final EmbeddedStorageManager storageManager)
		{
			super(storageManager);
		}
		
		@Override
		public void run()
		{
			final List<Item> items = this.data().items();
			items.add(new Item(this.item));
			this.storageManager.store(items);
			System.out.println("Item added");
		}
	}
	
	@Command(
		name = "remove",
		aliases = {"r"},
		description = "Removes an item at the specified index",
		mixinStandardHelpOptions = true
	)
	static class RemoveCommand extends Abstract
	{
		@Parameters(
			index = "0",
			description = "the item's index"
		)
		int index;
		
		RemoveCommand(final EmbeddedStorageManager storageManager)
		{
			super(storageManager);
		}
		
		@Override
		public void run()
		{
			final List<Item> items = this.data().items();
			if(this.index < 0 || this.index >= items.size())
			{
				System.out.println("Invalid index");
			}
			else
			{
				items.remove(this.index);
				this.storageManager.store(items);
				System.out.println("Item removed");
			}
		}
	}
	
	@Command(
		name = "search",
		aliases = {"s"},
		description = "Searches items",
		mixinStandardHelpOptions = true
	)
	static class SearchCommand extends Abstract
	{
		@Parameters(
			index = "0",
			description = "search term"
		)
		String term;
		
		SearchCommand(final EmbeddedStorageManager storageManager)
		{
			super(storageManager);
		}
		
		@Override
		public void run()
		{
			final List<Item> items = this.data().items().stream()
				.filter(item -> item.getTitle().toLowerCase().startsWith(this.term))
				.collect(Collectors.toList());
			this.print(items);
		}
	}
	
	@Command(
		name = "list",
		aliases = {"l"},
		description = "List all items",
		mixinStandardHelpOptions = true
	)
	static class ListCommand extends Abstract
	{
		ListCommand(final EmbeddedStorageManager storageManager)
		{
			super(storageManager);
		}
		
		@Override
		public void run()
		{
			this.print(this.data().items());
		}
	}

	@Command(
		name = "quit",
		aliases = {"q"},
		description = "Exits the program",
		mixinStandardHelpOptions = true
	)
	static class QuitCommand extends Abstract
	{
		QuitCommand(final EmbeddedStorageManager storageManager)
		{
			super(storageManager);
		}

		@Override
		public void run()
		{
			this.storageManager.shutdown();

			System.out.println("Bye!");

			System.exit(0);
		}
	}
	
}
