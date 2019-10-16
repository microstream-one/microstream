package one.microstream.persistence.types;

import java.lang.reflect.Field;

public interface PersistenceMemoryObjectValuesCopier<M>
{
	public void copyObjectValues(Object object, M target, PersistenceStoreHandler persister);
	
	
	
	public interface Creator<L>
	{
		public PersistenceMemoryObjectValuesCopier<L> createMemoryObjectValuesCopier(Field fields);
	}
	
}
