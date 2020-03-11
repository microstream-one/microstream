package one.microstream.storage.restclient.types;

import java.util.Collections;
import java.util.List;

public interface StorageViewValue extends StorageViewElement
{
	public static class Default extends StorageViewElement.Abstract implements StorageViewValue
	{
		Default(
			final StorageView.Default view,
			final String name,
			final String value
		)
		{
			super(view, name, value);
		}
		
		@Override
		public boolean hasMembers()
		{
			return false;
		}
		
		@Override
		public List<StorageViewElement> members(
			final boolean forceRefresh
		)
		{
			return Collections.emptyList();
		}
		
	}
	
}
