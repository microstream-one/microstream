package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceTypeHandlerLookup;

public interface BinaryTypeHandlerLookup extends PersistenceTypeHandlerLookup<Binary>
{
	@Override
	public <T> BinaryTypeHandler<T> lookupTypeHandler(Class<T> type);

	@Override
	public BinaryTypeHandler<?> lookupTypeHandler(long typeId);
}
