package one.microstream.collections.types;

import java.util.function.Function;

import one.microstream.collections.interfaces.ReleasingCollection;

public interface XReplacingCollection<E> extends ReleasingCollection<E>
{
	public long substitute(Function<? super E, ? extends E> mapper);
}
