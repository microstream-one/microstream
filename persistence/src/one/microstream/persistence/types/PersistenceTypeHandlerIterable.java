package one.microstream.persistence.types;

import java.util.function.Consumer;

public interface PersistenceTypeHandlerIterable<M>
{
	public <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(C iterator);
	
	public <C extends Consumer<? super PersistenceLegacyTypeHandler<M, ?>>> C iterateLegacyTypeHandlers(C iterator);
	
	public default <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateAllTypeHandlers(final C iterator)
	{
		this.iterateTypeHandlers(iterator);
		this.iterateLegacyTypeHandlers(iterator);
		
		return iterator;
	}
}
