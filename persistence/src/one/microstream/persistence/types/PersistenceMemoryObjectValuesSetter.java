package one.microstream.persistence.types;

import java.lang.reflect.Field;

public interface PersistenceMemoryObjectValuesSetter<M>
{
	public void setObjectValues(M medium, Object object, PersistenceObjectIdResolver idResolver);
	
	
	
	public interface Creator<M>
	{
		public PersistenceMemoryObjectValuesSetter<M> createMemoryObjectValuesSetter(Field... fields);
	}
	
}
