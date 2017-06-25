package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.traversal.handlers.TraversalHandlerLeaf;
import net.jadoth.traversal.TraversalHandler;

@FunctionalInterface
public interface TraversalHandlerCustomProvider<T>
{
	public default Class<T> handledType()
	{
		// generic implementation might not know its handled type.
		return null;
	}
	
	/*
	 * The extending is required to be compatible with polymorphic logic.
	 * E.g. implementions of Map are not exactly Map.class, but a sub type of it.
	 */
	public TraversalHandler<T> provideTraversalHandler(Class<? extends T> type, TraversalHandlingLogicProvider logicProvider);
		
	
	
	public static <E> TraversalHandlerCustomProvider<E> NewLeafHandlerProvider()
	{
		return new LeafHandlerProvider<>();
	}
	
	public final class LeafHandlerProvider<T> implements TraversalHandlerCustomProvider<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
		@Override
		public TraversalHandler<T> provideTraversalHandler(
			final Class<? extends T>             type         ,
			final TraversalHandlingLogicProvider logicProvider
		)
		{
			final Predicate<? super T> handlingLogic = logicProvider.provideHandlingLogic(type);
			
			// if the type is both leaf (no references to be traversed) and not handled by logic, it can be skipped
			return handlingLogic == null
				? null
				: TraversalHandlerLeaf.New(handlingLogic)
			;
		}
		
	}

}
