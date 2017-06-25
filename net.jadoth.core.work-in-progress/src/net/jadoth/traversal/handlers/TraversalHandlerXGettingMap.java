package net.jadoth.traversal.handlers;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingMap;
import net.jadoth.traversal.TraversalHandlerCustomProvider;
import net.jadoth.traversal.TraversalHandlingLogicProvider;
import net.jadoth.traversal.TraversalHandler;
import net.jadoth.util.KeyValue;


public class TraversalHandlerXGettingMap extends TraversalHandler.AbstractImplementation<XGettingMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings("all")
	public static final Class<XGettingMap<?, ?>> genericType()
	{
		// no idea how to get ".class" to work otherwise in conjunction with generics.
		return (Class)XGettingMap.class;
	}

	public static  TraversalHandlerXGettingMap New(final Predicate<? super XGettingMap<?, ?>> logic)
	{
		return new TraversalHandlerXGettingMap(
			notNull(logic)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected TraversalHandlerXGettingMap(final Predicate<? super XGettingMap<?, ?>> logic)
	{
		super(logic);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public final Class<XGettingMap<?, ?>> handledType()
	{
		return genericType();
	}

	@Override
	public void traverseReferences(final XGettingMap<?, ?> map, final Consumer<Object> referenceHandler)
	{
		for(final KeyValue<?, ?> entry : map)
		{
			referenceHandler.accept(entry.key());
			referenceHandler.accept(entry.value());
		}
	}
	
	
	
	public static final class Provider implements TraversalHandlerCustomProvider<XGettingMap<?, ?>>
	{
		@Override
		public final Class<XGettingMap<?, ?>> handledType()
		{
			return genericType();
		}
		
		@Override
		public TraversalHandler<XGettingMap<?, ?>> provideTraversalHandler(
			final Class<? extends XGettingMap<?, ?>> type         ,
			final TraversalHandlingLogicProvider     logicProvider
		)
		{
			return TraversalHandlerXGettingMap.New(
				logicProvider.provideHandlingLogic(type)
			);
		}
	}

}
