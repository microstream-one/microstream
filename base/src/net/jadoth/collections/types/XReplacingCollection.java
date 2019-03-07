package net.jadoth.collections.types;

import java.util.function.Function;

import net.jadoth.collections.interfaces.ReleasingCollection;

public interface XReplacingCollection<E> extends ReleasingCollection<E>
{
	public long substitute(Function<? super E, ? extends E> mapper);
}
