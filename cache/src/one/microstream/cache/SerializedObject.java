
package one.microstream.cache;

public interface SerializedObject
{
	public byte[] serializedData();
	
	public static SerializedObject New(final int hashCode, final byte[] serializedData)
	{
		return new Default(hashCode, serializedData);
	}
	
	public static class Default implements SerializedObject
	{
		private final int    hashCode;
		private final byte[] serializedData;
		
		Default(final int hashCode, final byte[] serializedData)
		{
			super();
			
			this.hashCode       = hashCode;
			this.serializedData = serializedData;
		}
		
		@Override
		public byte[] serializedData()
		{
			return this.serializedData;
		}
		
		@Override
		public int hashCode()
		{
			return this.hashCode;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			return obj == this ||
				(obj instanceof SerializedObject && obj.hashCode() == this.hashCode);
		}
	}
}
