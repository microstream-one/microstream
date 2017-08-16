package net.jadoth.traversal;

import net.jadoth.collections.types.XGettingSequence;

public final class HandlingPredicateSkippedTypesPolymorphic extends AbstractHandlingPredicate
{

	HandlingPredicateSkippedTypesPolymorphic(final XGettingSequence<Class<?>> skippedTypesPolymorphic)
	{
		super(null, skippedTypesPolymorphic, null);
	}

	@Override
	public final boolean test(final Object instance)
	{
		return !this.isSkippedTypePolymorphic(instance.getClass());
	}
	
}