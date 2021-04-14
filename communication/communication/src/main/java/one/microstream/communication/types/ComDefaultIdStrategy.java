package one.microstream.communication.types;

import one.microstream.persistence.internal.CompositeIdProvider;
import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceObjectIdStrategy;
import one.microstream.persistence.types.PersistenceTypeIdStrategy;

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
	
	public CompositeIdProvider createIdProvider()
	{
		return CompositeIdProvider.New(
			this.createTypeIdProvider(),
			this.createObjectIdProvider()
		);
	}
	
}
