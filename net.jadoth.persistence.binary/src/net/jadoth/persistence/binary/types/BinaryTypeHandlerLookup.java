package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceTypeHandlerLookup;

public interface BinaryTypeHandlerLookup extends PersistenceTypeHandlerLookup<Binary>
{
	@Override
	public <T> BinaryTypeHandler<T> lookupTypeHandler(Class<T> type);

	@Override
	public BinaryTypeHandler<?> lookupTypeHandler(long typeId);
}
