package one.microstream.persistence.types;

import java.util.function.Consumer;

public interface PersistenceTypeHandlerIterable<D>
{
	public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(C iterator);
	
	public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(C iterator);
	
	public default <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
	{
		this.iterateTypeHandlers(iterator);
		this.iterateLegacyTypeHandlers(iterator);
		
		return iterator;
	}
}
