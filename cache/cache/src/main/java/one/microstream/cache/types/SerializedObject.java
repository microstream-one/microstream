
package one.microstream.cache.types;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.types.Binary;


public interface SerializedObject extends ByteSized
{
	public Binary serializedData();
	
	public static SerializedObject New(final int hashCode, final Binary serializedData)
	{
		return new Default(hashCode, serializedData);
	}
	
	public static class Default implements SerializedObject
	{
		private final int    hashCode;
		private final Binary serializedData;
		
		Default(final int hashCode, final Binary serializedData)
		{
			super();
			
			this.hashCode       = hashCode;
			this.serializedData = notNull(serializedData);
		}
		
		@Override
		public Binary serializedData()
		{
			return this.serializedData;
		}
		
		@Override
		public long byteSize()
		{
			return this.serializedData.totalLength();
		}
		
		@Override
		public int hashCode()
		{
			return this.hashCode;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			return obj == this
				|| (   obj instanceof SerializedObject
				    && obj.hashCode() == this.hashCode
				   );
		}
		
	}
	
}
