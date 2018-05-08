package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceTypeHandler;

public interface BinaryTypeHandler<T> extends PersistenceTypeHandler<Binary, T>
{
	public abstract class AbstractImplementation<T>
	extends PersistenceTypeHandler.AbstractImplementation<Binary, T>
	implements BinaryTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final Class<T> type)
		{
			super(type);
		}

	}

}
