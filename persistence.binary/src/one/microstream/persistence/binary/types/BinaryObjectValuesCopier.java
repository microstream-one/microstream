package one.microstream.persistence.binary.types;

import java.lang.reflect.Field;

import one.microstream.persistence.types.PersistenceStoreHandler;

public interface BinaryObjectValuesCopier
{
	public void copyObjectValues(Object object, long targetAddress, PersistenceStoreHandler persister);
	
	
	
	public interface Creator
	{
		public BinaryObjectValuesCopier createMemoryObjectValuesCopier(Field fields);
	}
	
}
