package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;

public final class CompositeSwizzleIdProvider implements SwizzleObjectIdProvider, SwizzleTypeIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final SwizzleTypeIdProvider   typeIdProvider  ;
	private final SwizzleObjectIdProvider objectIdProvider;

	private transient boolean initialized;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public CompositeSwizzleIdProvider(
		final SwizzleTypeIdProvider   typeIdProvider  ,
		final SwizzleObjectIdProvider objectIdProvider
	)
	{
		super();
		this.typeIdProvider   = notNull(typeIdProvider  );
		this.objectIdProvider = notNull(objectIdProvider);
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
