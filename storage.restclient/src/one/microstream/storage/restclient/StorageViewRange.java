package one.microstream.storage.restclient;

import java.util.List;
import java.util.function.Function;

public interface StorageViewRange extends StorageViewElement
{
	public long offset();
	
	public long length();
	
	
	public static class Default extends StorageViewElement.Default implements StorageViewRange
	{
		private final long offset;
		private final long length;
		
		Default(
			final StorageViewElement parent,
			final String name,
			final int offset,
			final int length,
			final Function<StorageViewElement, List<StorageViewElement>> membersSupplier
		)
		{
			super(parent, name, null, membersSupplier);
			
			this.offset = offset;
			this.length = length;
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
		
	}
	
}