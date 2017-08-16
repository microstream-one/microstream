package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSet;

public final class HandlingPredicateSkippedTypesCustom extends AbstractHandlingPredicate
{

	HandlingPredicateSkippedTypesCustom(
		final XGettingSet<Class<?>> skippedTypes     ,
		final Predicate<Object>     handlingPredicate
	)
	{
		super(skippedTypes, null, handlingPredicate);
	}

	@Override
	public final boolean test(final Object instance)
	{
		return !this.isSkippedType(instance.getClass())
			&& this.isHandledCustom(instance)
		;
	}
	
}