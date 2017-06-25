package net.jadoth.traversal.handlers;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.traversal.TraversalHandlerCustomProvider;
import net.jadoth.traversal.TraversalHandlingLogicProvider;
import net.jadoth.traversal.TraversalHandler;

public final class TraversalHandlerIterable extends TraversalHandler.AbstractImplementation<Iterable<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings("all")
	public static final Class<Iterable<?>> genericType()
	{
		// no idea how to get ".class" to work otherwise in conjunction with generics.
		return (Class)Iterable.class;
	}

	public static TraversalHandlerIterable New(final Predicate<? super Iterable<?>> logic)
	{
		return new TraversalHandlerIterable(logic); // logic may be null
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected TraversalHandlerIterable(final Predicate<? super Iterable<?>> logic)
	{
		super(logic);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public final Class<Iterable<?>> handledType()
	{
		return genericType();
	}

	@Override
	public final void traverseReferences(final Iterable<?> iterable, final Consumer<Object> referenceHandler)
	{
		for(final Object element : iterable)
		{
			referenceHandler.accept(element);
		}
	}
	
	
	
	public static final class Provider implements TraversalHandlerCustomProvider<Iterable<?>>
	{
		@Override
		public final Class<Iterable<?>> handledType()
		{
			return genericType();
		}
		
		@Override
		public TraversalHandler<Iterable<?>> provideTraversalHandler(
			final Class<? extends Iterable<?>>   type         ,
			final TraversalHandlingLogicProvider logicProvider
		)
		{
			return TraversalHandlerIterable.New(
				logicProvider.provideHandlingLogic(type)
			);
		}
	}

}
