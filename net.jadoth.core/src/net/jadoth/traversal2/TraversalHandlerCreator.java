package net.jadoth.traversal2;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public interface TraversalHandlerCreator
{
	public TraversalHandler createTraversalHandler(Class<?> type);
	
	
	
	public final class Reflective implements TraversalHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Predicate<? super Field> fieldSelector;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Reflective(final Predicate<? super Field> fieldSelector)
		{
			super();
			this.fieldSelector = fieldSelector;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private final Field[] collectFields(final Class<?> type)
		{
			// FIXME TraversalHandlerCreator.Reflective#createTraversalHandler()
			throw new net.jadoth.meta.NotImplementedYetError();
		}

		@Override
		public TraversalHandler createTraversalHandler(final Class<?> type)
		{
			return new TraverserReflective(this.collectFields(type));
		}
		
	}
	
}
