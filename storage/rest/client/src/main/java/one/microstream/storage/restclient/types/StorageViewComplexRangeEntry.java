package one.microstream.storage.restclient.types;

import java.util.List;

public interface StorageViewComplexRangeEntry extends StorageViewElement
{
	public static class Default extends StorageViewElement.Abstract implements StorageViewComplexRangeEntry
	{
		private final List<StorageViewElement> members;
		
		Default(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final String value,
			final List<StorageViewElement> members
		)
		{
			super(view, parent, name, value, null);
			
			this.members = members;
		}
		
		@Override
		public boolean hasMembers()
		{
			return this.members.size() > 0;
		}
		
		@Override
		public List<StorageViewElement> members(
			final boolean forceRefresh
		)
		{
			return this.members;
		}
		
	}
	
}
