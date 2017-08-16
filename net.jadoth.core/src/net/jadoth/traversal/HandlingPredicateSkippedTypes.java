package net.jadoth.traversal;

import net.jadoth.collections.types.XGettingSet;

public final class HandlingPredicateSkippedTypes extends AbstractHandlingPredicate
{

	HandlingPredicateSkippedTypes(final XGettingSet<Class<?>> skippedTypes)
	{
		super(skippedTypes, null, null);
	}

	@Override
	public final boolean test(final Object instance)
	{
		return !this.isSkippedType(instance.getClass());
	}
	
}