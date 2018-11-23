package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceObjectIdProvider;
import net.jadoth.persistence.types.PersistenceTypeIdProvider;

public final class CompositeSwizzleIdProvider implements PersistenceObjectIdProvider, PersistenceTypeIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static CompositeSwizzleIdProvider New(
		final PersistenceTypeIdProvider   typeIdProvider  ,
		final PersistenceObjectIdProvider objectIdProvider
	)
	{
		return new CompositeSwizzleIdProvider(
			notNull(typeIdProvider)  ,
			notNull(objectIdProvider)
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PersistenceTypeIdProvider   typeIdProvider  ;
	private final PersistenceObjectIdProvider objectIdProvider;

	private transient boolean initialized;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	CompositeSwizzleIdProvider(
		final PersistenceTypeIdProvider   typeIdProvider  ,
		final PersistenceObjectIdProvider objectIdProvider
	)
	{
		super();
		this.typeIdProvider   = typeIdProvider  ;
		this.objectIdProvider = objectIdProvider;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void markInitialized()
	{
		this.initialized = true;
	}

	private boolean isInitialized()
	{
		return this.initialized;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public final synchronized CompositeSwizzleIdProvider initialize()
	{
		if(!this.isInitialized())
		{
			this.typeIdProvider.initializeTypeId();
			this.objectIdProvider.initializeObjectId();
			this.markInitialized();
		}
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final CompositeSwizzleIdProvider initializeTypeId()
	{
		return this.initialize();
	}

	@Override
	public final CompositeSwizzleIdProvider initializeObjectId()
	{
		return this.initialize();
	}

	@Override
	public final long currentObjectId()
	{
		return this.objectIdProvider.currentObjectId();
	}

	@Override
	public final long currentTypeId()
	{
		return this.typeIdProvider.currentTypeId();
	}

	@Override
	public final long provideNextTypeId()
	{
		return this.typeIdProvider.provideNextTypeId();
	}

	@Override
	public final long provideNextObjectId()
	{
		return this.objectIdProvider.provideNextObjectId();
	}

	@Override
	public final CompositeSwizzleIdProvider updateCurrentObjectId(final long currentObjectId)
	{
		this.objectIdProvider.updateCurrentObjectId(currentObjectId);
		return this;
	}

	@Override
	public final CompositeSwizzleIdProvider updateCurrentTypeId(final long currentTypeId)
	{
		this.typeIdProvider.updateCurrentTypeId(currentTypeId);
		return this;
	}

}
