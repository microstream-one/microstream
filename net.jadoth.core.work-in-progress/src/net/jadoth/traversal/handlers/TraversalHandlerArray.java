package net.jadoth.traversal.handlers;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.traversal.TraversalHandlerCustomProvider;
import net.jadoth.traversal.TraversalHandlingLogicProvider;
import net.jadoth.traversal.TraversalHandler;

public final class TraversalHandlerArray extends TraversalHandler.AbstractImplementation<Object[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static TraversalHandlerArray New(final Predicate<? super Object[]> logic)
	{
		return new TraversalHandlerArray(logic); // logic may be null
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraversalHandlerArray(final Predicate<? super Object[]> logic)
	{
		super(logic);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public final Class<Object[]> handledType()
	{
		return Object[].class;
	}

	@Override
	public final void traverseReferences(final Object[] array, final Consumer<Object> referenceHandler)
	{
		for(final Object element : array)
		{
			referenceHandler.accept(element);
		}
	}
	
	
	
	public static final class Provider implements TraversalHandlerCustomProvider<Object[]>
	{
		@Override
		public final Class<Object[]> handledType()
		{
			return Object[].class;
		}
		
		@Override
		public TraversalHandler<Object[]> provideTraversalHandler(
			final Class<? extends Object[]>      type         ,
			final TraversalHandlingLogicProvider logicProvider
		)
		{
			return TraversalHandlerArray.New(
				logicProvider.provideHandlingLogic(type)
			);
		}
	}

}
