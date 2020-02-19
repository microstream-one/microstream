
package one.microstream.storage.restclient;

public interface StorageViewConfiguration
{
	public long elementRangeMaximumLength();
	
	
	public static StorageViewConfiguration Default()
	{
		return new StorageViewConfiguration.Default(100);
	}
	
	
	public static class Default implements StorageViewConfiguration
	{
		private final long elementRangeMaximumLength;

		Default(
			final long elementRangeMaximumLength
		)
		{
			super();
			this.elementRangeMaximumLength = elementRangeMaximumLength;
		}
		
		@Override
		public long elementRangeMaximumLength()
		{
			return this.elementRangeMaximumLength;
		}
		
	}
	
}
