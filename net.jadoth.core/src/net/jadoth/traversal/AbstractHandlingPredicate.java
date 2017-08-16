package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;

public abstract class AbstractHandlingPredicate implements Predicate<Object>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final XGettingSet<Class<?>>      skippedTypes           ;
	private final XGettingSequence<Class<?>> skippedTypesPolymorphic;
	private final Predicate<Object>          handlingPredicate      ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractHandlingPredicate(
		final XGettingSet<Class<?>>      skippedTypes           ,
		final XGettingSequence<Class<?>> skippedTypesPolymorphic,
		final Predicate<Object>          handlingPredicate
	)
	{
		super();
		this.skippedTypes            = skippedTypes           ;
		this.skippedTypesPolymorphic = skippedTypesPolymorphic;
		this.handlingPredicate       = handlingPredicate      ;
	}
	
	final boolean isSkippedType(final Class<?> type)
	{
		return this.skippedTypes.contains(type);
	}
	
	final boolean isSkippedTypePolymorphic(final Class<?> type)
	{
		return this.skippedTypesPolymorphic.containsSearched(t -> t.isAssignableFrom(type));
	}
	
	final boolean isHandledCustom(final Object instance)
	{
		return this.handlingPredicate.test(instance);
	}
	
}