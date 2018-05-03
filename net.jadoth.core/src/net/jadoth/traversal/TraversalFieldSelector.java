package net.jadoth.traversal;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public interface TraversalFieldSelector
{
	// Field only knows its decalaring class, not the actual class, which can be very important for making the decision.
	public boolean test(Class<?> actualClass, Field field);
	
	
	
	public static TraversalFieldSelector New(final Predicate<? super Field> simplePredicate)
	{
		return new TraversalFieldSelector.Implementation(
			notNull(simplePredicate)
		);
	}
	
	public final class Implementation implements TraversalFieldSelector
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Predicate<? super Field> simplePredicate;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Implementation(final Predicate<? super Field> simplePredicate)
		{
			super();
			this.simplePredicate = simplePredicate;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean test(final Class<?> actualClass, final Field field)
		{
			return this.simplePredicate.test(field);
		}
		
		
	}
}
