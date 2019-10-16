package one.microstream.persistence.types;

import java.lang.reflect.Field;

public interface PersistenceMemoryObjectValuesHandler<M>
extends PersistenceMemoryObjectValuesCopier<M>, PersistenceMemoryObjectValuesSetter<M>
{
	public interface Creator<M>
	extends
	PersistenceMemoryObjectValuesCopier.Creator<M>,
	PersistenceMemoryObjectValuesSetter.Creator<M>
	{
		@Override
		public PersistenceMemoryObjectValuesCopier<M> createMemoryObjectValuesCopier(Field fields);
		
		@Override
		public PersistenceMemoryObjectValuesSetter<M> createMemoryObjectValuesSetter(Field... fields);

		public PersistenceMemoryObjectValuesHandler<M> createMemoryObjectValuesHandler(Field... fields);
	}
	
}
