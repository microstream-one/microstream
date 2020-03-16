package one.microstream.storage.restclient.types;

import java.util.List;

public interface StorageViewRange extends StorageViewElement
{
	public long offset();
	
	public long length();
	
	
	public static class Default extends StorageViewElement.Abstract implements StorageViewRange
	{
		private final long               objectId;
		private final long               offset;
		private final long               length;
		private List<StorageViewElement> members;
		
		Default(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final long objectId,
			final long offset,
			final long length
		)
		{
			super(view, parent, name, null, null);
			this.objectId = objectId;
			this.offset   = offset;
			this.length   = length;
		}

		@Override
		public long offset()
		{
			return this.offset;
		}

		@Override
		public long length()
		{
			return this.length;
		}
		
		@Override
		public boolean hasMembers()
		{
			return true;
		}
		
		@Override
		public List<StorageViewElement> members(final boolean forceRefresh)
		{
			if(this.members == null || forceRefresh)
			{
				this.members = this.view().variableMembers(
					this,
					this.objectId,
					this.offset,
					this.length
				);
			}
			return this.members;
		}
		
	}
	
}