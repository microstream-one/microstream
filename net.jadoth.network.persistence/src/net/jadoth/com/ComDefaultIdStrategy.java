package net.jadoth.com;

import net.jadoth.persistence.internal.CompositeSwizzleIdProvider;
import net.jadoth.persistence.types.PersistenceIdStrategy;
import net.jadoth.persistence.types.PersistenceObjectIdStrategy;
import net.jadoth.persistence.types.PersistenceTypeIdStrategy;

public final class ComDefaultIdStrategy implements PersistenceIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static ComDefaultIdStrategy New(final long startingObjectId)
	{
		return new ComDefaultIdStrategy(
			PersistenceTypeIdStrategy.None(),
			PersistenceObjectIdStrategy.Transient(startingObjectId)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeIdStrategy.None        typeIdStrategy  ;
	private final PersistenceObjectIdStrategy.Transient objectIdStrategy;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ComDefaultIdStrategy(
		final PersistenceTypeIdStrategy.None        typeIdStrategy  ,
		final PersistenceObjectIdStrategy.Transient objectIdStrategy
	)
	{
		super();
		this.typeIdStrategy   = typeIdStrategy  ;
		this.objectIdStrategy = objectIdStrategy;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public PersistenceObjectIdStrategy.Transient objectIdStragegy()
	{
		return this.objectIdStrategy;
	}
	
	@Override
	public PersistenceTypeIdStrategy.None typeIdStragegy()
	{
		return this.typeIdStrategy;
	}
	
	public final long startingObjectId()
	{
		return this.objectIdStragegy().startingObjectId();
	}
	
	public CompositeSwizzleIdProvider createIdProvider()
	{
		return CompositeSwizzleIdProvider.New(
			this.createTypeIdProvider(),
			this.createObjectIdProvider()
		);
	}
	
}
