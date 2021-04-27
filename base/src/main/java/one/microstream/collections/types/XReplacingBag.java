package one.microstream.collections.types;

import java.util.function.Function;
import java.util.function.Predicate;


public interface XReplacingBag<E> extends XGettingCollection<E>, XReplacingCollection<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XReplacingBag<E> newInstance();
	}

	/**
	 * Replaces the first element that is equal to the given element
	 * with the replacement and then returns true.
	 * 
	 * @param element to replace
	 * @param replacement for the found element
	 * @return {@code true} if element is found, {@code false} if not
	 */
	public boolean replaceOne(E element, E replacement);

	public long replace(E element, E replacement);

	public long replaceAll(XGettingCollection<? extends E> elements, E replacement);

	public boolean replaceOne(Predicate<? super E> predicate, E replacement);
	
	public long replace(Predicate<? super E> predicate, E replacement);
		
	public long substitute(Predicate<? super E> predicate, Function<E, E> mapper);

	@Override
	public XReplacingBag<E> copy();
}
