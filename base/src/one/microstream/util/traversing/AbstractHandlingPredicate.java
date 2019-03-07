package one.microstream.util.traversing;

import java.util.function.Predicate;

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingSet;

public abstract class AbstractHandlingPredicate implements Predicate<Object>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Predicate<Object>          customPredicate ;
	private final HashEnum<Class<?>>         positiveTypes   ;
	private final HashEnum<Class<?>>         negativeTypes   ;
	private final XGettingSequence<Class<?>> typesPolymorphic;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractHandlingPredicate(
		final Predicate<Object>          customPredicate ,
		final XGettingSet<Class<?>>      positiveTypes   ,
		final XGettingSequence<Class<?>> typesPolymorphic
	)
	{
		super();
		this.customPredicate  = customPredicate ;
		this.positiveTypes    = HashEnum.New(positiveTypes);
		this.negativeTypes    = HashEnum.New()  ;
		this.typesPolymorphic = typesPolymorphic;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final boolean test(final Object instance)
	{
		if(this.customPredicate != null && this.customPredicate.test(instance))
		{
			return true;
		}
		
		final Class<?> type = instance.getClass();
		if(this.positiveTypes.contains(type))
		{
			return true;
		}
		if(this.negativeTypes.contains(type))
		{
			return false;
		}
		for(final Class<?> tp : this.typesPolymorphic)
		{
			if(tp.isAssignableFrom(type))
			{
				this.positiveTypes.add(type);
				return true;
			}
		}
		this.negativeTypes.add(type);
		return false;
	}
	
}
