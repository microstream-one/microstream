package net.jadoth.traversal.handlers;

import static net.jadoth.Jadoth.notNull;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.traversal.TraversalHandlerCustomProvider;
import net.jadoth.traversal.TraversalHandlingLogicProvider;
import net.jadoth.traversal.TraversalHandler;


public final class TraversalHandlerMap extends TraversalHandler.AbstractImplementation<Map<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings("all")
	public static final Class<Map<?, ?>> genericType()
	{
		// no idea how to get ".class" to work otherwise in conjunction with generics.
		return (Class)Map.class;
	}

	public static TraversalHandlerMap New(final Predicate<? super Map<?, ?>> logic)
	{
		return new TraversalHandlerMap(
			notNull(logic)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected TraversalHandlerMap(final Predicate<? super Map<?, ?>> logic)
	{
		super(logic);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public final Class<Map<?, ?>> handledType()
	{
		return genericType();
	}

	@Override
	public final void traverseReferences(final Map<?, ?> map, final Consumer<Object> referenceHandler)
	{
		for(final Map.Entry<?, ?> entry : map.entrySet())
		{
			referenceHandler.accept(entry.getKey());
			referenceHandler.accept(entry.getValue());
		}
	}

	
	
	public static final class Provider implements TraversalHandlerCustomProvider<Map<?, ?>>
	{
		@Override
		public final Class<Map<?, ?>> handledType()
		{
			return genericType();
		}
		
		@Override
		public TraversalHandler<Map<?, ?>> provideTraversalHandler(
			final Class<? extends Map<?, ?>>     type         ,
			final TraversalHandlingLogicProvider logicProvider
		)
		{
			return TraversalHandlerMap.New(
				logicProvider.provideHandlingLogic(type)
			);
		}
	}

}
