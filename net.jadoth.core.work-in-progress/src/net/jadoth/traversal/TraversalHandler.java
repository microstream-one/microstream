package net.jadoth.traversal;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.functional.JadothPredicates;
import net.jadoth.traversal.TraversalHandler;

@FunctionalInterface
public interface TraversalHandler<T>
{
	public default Class<T> handledType()
	{
		// generic implementation might not know its handled type.
		return null;
	}
	
	public default boolean handleObject(final T instance, final Consumer<Object> referenceHandler)
	{
		return false;
	}

	public void traverseReferences(T instance, Consumer<Object> referenceHandler);


	
	public abstract class AbstractImplementation<T> implements TraversalHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final Predicate<? super T> logic;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected AbstractImplementation(final Predicate<? super T> logic)
		{
			super();
			this.logic = logic != null ? logic : JadothPredicates.none();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////
		
		public final Predicate<? super T> logic()
		{
			return this.logic;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
		@Override
		public final boolean handleObject(final T instance, final Consumer<Object> referenceHandler)
		{
			return this.logic.test(instance);
		}
		
	}

}
