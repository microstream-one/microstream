
package one.microstream.storage.restclient;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;



public interface StorageViewElement
{
	public StorageViewElement parent();
	
	public String name();
	
	public String value();
	
	public boolean hasMembers();
	
	public List<StorageViewElement> members(boolean forceRefresh);
	
	
	public static class Default implements StorageViewElement
	{
		private final StorageViewElement parent;
		private final String                                                 name;
		private final String                                                 value;
		private final Function<StorageViewElement, List<StorageViewElement>> membersSupplier;
		private List<StorageViewElement>                                     members;
		
		Default(
			final StorageViewElement parent,
			final String name,
			final String value
		)
		{
			this(parent, name, value, null);
		}
		
		Default(
			final StorageViewElement parent,
			final String name,
			final String value,
			final Function<StorageViewElement, List<StorageViewElement>> membersSupplier
		)
		{
			super();
			this.parent = parent;
			this.name  = name;
			this.value = value;
			this.membersSupplier = membersSupplier;
		}
		
		Default(
			final StorageViewElement parent,
			final String name,
			final String value,
			final Function<StorageViewElement, List<StorageViewElement>> membersSupplier,
			final List<StorageViewElement> members
		)
		{
			this(parent, name, value, membersSupplier);
			this.members = members;
		}
		
		@Override
		public StorageViewElement parent()
		{
			return this.parent;
		}
		
		@Override
		public String name()
		{
			return this.name;
		}
		
		@Override
		public String value()
		{
			return this.value;
		}
		
		@Override
		public boolean hasMembers()
		{
			return this.membersSupplier != null;
		}
		
		@Override
		public List<StorageViewElement> members(final boolean forceRefresh)
		{
			if(this.membersSupplier == null)
			{
				return Collections.emptyList();
			}
			if(this.members == null || forceRefresh)
			{
				this.members = this.membersSupplier.apply(this);
			}
			return this.members;
		}
		
	}
	
}
