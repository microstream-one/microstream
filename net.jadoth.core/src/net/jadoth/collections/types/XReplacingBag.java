package net.jadoth.collections.types;

import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.interfaces.ReleasingCollection;


public interface XReplacingBag<E> extends XGettingCollection<E>, ReleasingCollection<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XReplacingBag<E> newInstance();
	}

	public boolean replaceOne(E element, E replacement);

	public long replace(E element, E replacement);

	public long replaceAll(XGettingCollection<? extends E> elements, E replacement);

	public boolean replaceOne(Predicate<? super E> predicate, E substitute);
	
	public long replace(Predicate<? super E> predicate, E substitute);
		
	public long substitute(Function<E, E> mapper);

	public long substitute(Predicate<? super E> predicate, Function<E, E> mapper);

	@Override
	public XReplacingBag<E> copy();
}
