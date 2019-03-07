package net.jadoth.persistence.types;

import java.util.function.Consumer;

public interface PersistenceTypeHandlerIterable<M>
{
	public <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(C iterator);
}
