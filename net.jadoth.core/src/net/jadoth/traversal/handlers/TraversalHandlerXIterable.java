package net.jadoth.traversal.handlers;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.XIterable;
import net.jadoth.traversal.TraversalHandler;
import net.jadoth.traversal.TraversalHandlerCustomProvider;
import net.jadoth.traversal.TraversalHandlingLogicProvider;

public final class TraversalHandlerXIterable extends TraversalHandler.AbstractImplementation<XIterable<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings("all")
	public static final Class<XIterable<?>> genericType()
	{
		// no idea how to get ".class" to work otherwise in conjunction with generics.
		return (Class)XIterable.class;
	}

	public static TraversalHandlerXIterable New(final Predicate<? super XIterable<?>> logic)
	{
		return new TraversalHandlerXIterable(logic); // logic may be null
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected TraversalHandlerXIterable(final Predicate<? super XIterable<?>> logic)
	{
		super(logic);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public final Class<XIterable<?>> handledType()
	{
		return genericType();
	}

	@Override
	public final void traverseReferences(final XIterable<?> iterable, final Consumer<Object> referenceHandler)
	{
		iterable.iterate(referenceHandler);
	}
	
	public static final class Provider implements TraversalHandlerCustomProvider<XIterable<?>>
	{
		@Override
		public final Class<XIterable<?>> handledType()
		{
			return genericType();
		}
		
		@Override
		public TraversalHandler<XIterable<?>> provideTraversalHandler(
			final Class<? extends XIterable<?>>  type         ,
			final TraversalHandlingLogicProvider logicProvider
		)
		{
			return TraversalHandlerXIterable.New(
				logicProvider.provideHandlingLogic(type)
			);
		}
	}

}
