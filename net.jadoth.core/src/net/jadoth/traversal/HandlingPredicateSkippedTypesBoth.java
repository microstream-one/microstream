package net.jadoth.traversal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;

public final class HandlingPredicateSkippedTypesBoth extends AbstractHandlingPredicate
{

	HandlingPredicateSkippedTypesBoth(
		final XGettingSet<Class<?>>      skippedTypes           ,
		final XGettingSequence<Class<?>> skippedTypesPolymorphic
	)
	{
		super(skippedTypes, skippedTypesPolymorphic, null);
	}

	@Override
	public final boolean test(final Object instance)
	{
		return !this.isSkippedType(instance.getClass())
			&& !this.isSkippedTypePolymorphic(instance.getClass())
		;
	}
	
}