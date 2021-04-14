package one.microstream.collections.sorting;

import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Composite type to guarantee that the implementation of {@link Sortable} and {@link Consumer} refers to the same
 * parametrized type.
 *
 * 
 * @param <E>
 */
public interface SortableProcedure<E> extends Sortable<E>, Consumer<E>
{
	public static <E> void sortIfApplicable(final Consumer<E> procedure, final Comparator<? super E> comparator)
	{
		if(comparator == null || !(procedure instanceof SortableProcedure<?>))
		{
			return;
		}
		((SortableProcedure<E>)procedure).sort(comparator);
	}
}
