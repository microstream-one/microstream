package net.jadoth.persistence.types;

import java.util.function.Consumer;

public interface PersistenceTypeHandlerIterable<M>
{
	public void iterateTypeHandlers(Consumer<? super PersistenceTypeHandler<M, ?>> procedure);
}
