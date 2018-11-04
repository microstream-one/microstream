package net.jadoth.swizzling.types;

import static net.jadoth.X.notNull;

public interface SwizzleIdStrategy
{
	public SwizzleObjectIdStrategy objectIdStragegy();
	
	public SwizzleTypeIdStrategy typeIdStragegy();
	
	
	
	public static SwizzleIdStrategy New(
		final SwizzleObjectIdStrategy objectIdStrategy,
		final SwizzleTypeIdStrategy   typeIdStrategy
	)
	{
		return new SwizzleIdStrategy.Implementation(
			notNull(objectIdStrategy),
			notNull(typeIdStrategy)
		);
	}
	
	public class Implementation implements SwizzleIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final SwizzleObjectIdStrategy objectIdStrategy;
		private final SwizzleTypeIdStrategy   typeIdStrategy  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final SwizzleObjectIdStrategy objectIdStrategy,
			final SwizzleTypeIdStrategy   typeIdStrategy
		)
		{
			super();
			this.objectIdStrategy = objectIdStrategy;
			this.typeIdStrategy   = typeIdStrategy  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public SwizzleObjectIdStrategy objectIdStragegy()
		{
			return this.objectIdStrategy;
		}
		
		@Override
		public SwizzleTypeIdStrategy typeIdStragegy()
		{
			return this.typeIdStrategy;
		}
		
	}
	
}
