package net.jadoth.com;

import net.jadoth.swizzling.internal.CompositeSwizzleIdProvider;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.swizzling.types.SwizzleObjectIdStrategy;
import net.jadoth.swizzling.types.SwizzleTypeIdStrategy;

public final class ComDefaultIdStrategy implements SwizzleIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static ComDefaultIdStrategy New(final long startingObjectId)
	{
		return new ComDefaultIdStrategy(
			SwizzleTypeIdStrategy.None(),
			SwizzleObjectIdStrategy.Transient(startingObjectId)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final SwizzleTypeIdStrategy.None        typeIdStrategy  ;
	private final SwizzleObjectIdStrategy.Transient objectIdStrategy;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ComDefaultIdStrategy(
		final SwizzleTypeIdStrategy.None        typeIdStrategy  ,
		final SwizzleObjectIdStrategy.Transient objectIdStrategy
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
	public SwizzleObjectIdStrategy.Transient objectIdStragegy()
	{
		return this.objectIdStrategy;
	}
	
	@Override
	public SwizzleTypeIdStrategy.None typeIdStragegy()
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
