
package one.microstream.storage.restclient;

import java.util.List;



public interface StorageViewElement
{
	public StorageView view();
	
	public String name();
	
	public String value();
	
	public boolean hasMembers();
	
	public List<StorageViewElement> members(boolean forceRefresh);
	
	
	public static abstract class Abstract implements StorageViewElement
	{
		private final StorageView.Default view;
		private final String              name;
		private final String              value;
		
		Abstract(
			final StorageView.Default view,
			final String name,
			final String value
		)
		{
			this(view, name, value, null);
		}
		
		Abstract(
			final StorageView.Default view,
			final String name,
			final String value,
			final List<StorageViewElement> members
		)
		{
			super();
			this.view = view;
			this.name  = name;
			this.value = value;
		}
		
		@Override
		public StorageView.Default view()
		{
			return this.view;
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
		public String toString()
		{
			if(this.name != null && this.name.length() > 0)
			{
				return this.value != null && this.value.length() > 0
					? this.name + " = " + this.value
					: this.name
				;
			}
			
			return super.toString();
		}
		
	}
	
}
