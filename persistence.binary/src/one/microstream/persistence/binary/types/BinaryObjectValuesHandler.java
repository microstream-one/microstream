package one.microstream.persistence.binary.types;

import java.lang.reflect.Field;

public interface BinaryObjectValuesHandler
extends BinaryObjectValuesCopier, BinaryObjectValuesSetter
{
	public interface Creator extends BinaryObjectValuesCopier.Creator, BinaryObjectValuesSetter.Creator
	{
		@Override
		public BinaryObjectValuesCopier createMemoryObjectValuesCopier(Field fields);
		
		@Override
		public BinaryObjectValuesSetter createMemoryObjectValuesSetter(Field... fields);

		public BinaryObjectValuesHandler createMemoryObjectValuesHandler(Field... fields);
	}
	
}
