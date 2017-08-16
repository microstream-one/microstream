package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSequence;

public final class HandlingPredicateSkippedTypesPolymorphicCustom extends AbstractHandlingPredicate
{

	HandlingPredicateSkippedTypesPolymorphicCustom(
		final XGettingSequence<Class<?>> skippedTypesPolymorphic,
		final Predicate<Object>          handlingPredicate
	)
	{
		super(null, skippedTypesPolymorphic, handlingPredicate);
	}

	@Override
	public final boolean test(final Object instance)
	{
		return !this.isSkippedTypePolymorphic(instance.getClass())
			&& this.isHandledCustom(instance)
		;
	}
	
}