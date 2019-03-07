package one.microstream.collections;

import java.util.function.BiConsumer;
import java.util.function.Function;

import one.microstream.collections.interfaces.ChainStorage;


public abstract class AbstractChainStorage<E, K, V, EN extends AbstractChainEntry<E, K, V, EN>>
implements ChainStorage<E, K, V, EN>
{
	protected abstract EN head();

	protected abstract void disjoinEntry(EN entry);

	protected abstract boolean moveToStart(EN entry);

	protected abstract boolean moveToEnd(EN entry);
	
	protected abstract void replace(EN doomedEntry, EN keptEntry);
	
	protected abstract long substitute(Function<? super E, ? extends E> mapper, BiConsumer<EN, E> callback);
}
