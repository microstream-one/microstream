package net.jadoth.util.traversing;

import static net.jadoth.X.notNull;

import java.util.function.Function;

import net.jadoth.collections.EqHashEnum;


public final class Deduplicator implements Function<Object, Object>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static Deduplicator New()
	{
		return New(
			EqHashEnum.New()
		);
	}
	
	public static Deduplicator New(final EqHashEnum<Object> registry)
	{
		return new Deduplicator(
			notNull(registry)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final EqHashEnum<Object> registry;
	
	
		
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	Deduplicator(final EqHashEnum<Object> registry)
	{
		super();
		this.registry = registry;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Object apply(final Object instance)
	{
		return this.registry.deduplicate(instance);
	}

}
