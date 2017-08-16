package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;

public final class HandlingPredicateAll extends AbstractHandlingPredicate
{

	HandlingPredicateAll(
		final XGettingSet<Class<?>>      skippedTypes           ,
		final XGettingSequence<Class<?>> skippedTypesPolymorphic,
		final Predicate<Object>          handlingPredicate
	)
	{
		super(skippedTypes, skippedTypesPolymorphic, handlingPredicate);
	}

	@Override
	public final boolean test(final Object instance)
	{
		return !this.isSkippedType(instance.getClass())
			&& !this.isSkippedTypePolymorphic(instance.getClass())
			&& this.isHandledCustom(instance)
		;
	}
	
}