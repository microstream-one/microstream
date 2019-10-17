package one.microstream.persistence.binary.types;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import one.microstream.persistence.types.PersistenceObjectIdResolver;

public interface BinaryObjectValuesSetter
{
	public void setObjectValues(
		ByteBuffer                  source      ,
		long                        sourceOffset,
		Object                      object      ,
		PersistenceObjectIdResolver idResolver
	);
	
	
	
	public interface Creator
	{
		public BinaryObjectValuesSetter createMemoryObjectValuesSetter(Field... fields);
	}
	
}
