package net.jadoth.traversal.handlers;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.traversal.TraversalHandler;


public final class TraversalHandlerLeaf<T> extends TraversalHandler.AbstractImplementation<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static <T> TraversalHandlerLeaf<T> New(final Predicate<? super T> logic)
	{
		return new TraversalHandlerLeaf<>(logic); // logic may be null
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected TraversalHandlerLeaf(final Predicate<? super T> logic)
	{
		super(logic);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void traverseReferences(final T iterable, final Consumer<Object> referenceHandler)
	{
		// leaf types don't have references
	}

}
