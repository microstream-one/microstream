package one.microstream.persistence.binary.types;

import java.lang.reflect.Field;

import one.microstream.persistence.types.PersistenceObjectIdResolver;

public interface BinaryObjectValuesSetter
{
	public void setObjectValues(long sourceAddress, Object object, PersistenceObjectIdResolver idResolver);
	
	
	
	public interface Creator
	{
		public BinaryObjectValuesSetter createMemoryObjectValuesSetter(Field... fields);
	}
	
}
