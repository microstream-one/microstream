
package one.microstream.storage.restclient;

public interface StorageViewConfiguration
{
	public int elementRangeMaximumLength();
	
	
	public static StorageViewConfiguration Default()
	{
		return new StorageViewConfiguration.Default(100);
	}
	
	
	public static class Default implements StorageViewConfiguration
	{
		private final int elementRangeMaximumLength;

		Default(
			final int elementRangeMaximumLength
		)
		{
			super();
			this.elementRangeMaximumLength = elementRangeMaximumLength;
		}
		
		@Override
		public int elementRangeMaximumLength()
		{
			return this.elementRangeMaximumLength;
		}
		
	}
	
}
