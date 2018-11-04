package net.jadoth.swizzling.types;

public interface SwizzleObjectIdStrategy
{
	public SwizzleObjectIdProvider createObjectIdProvider();
	
	
	
	public static SwizzleObjectIdStrategy.Transient Transient()
	{
		return new SwizzleObjectIdStrategy.Transient(Swizzle.defaultStartObjectId());
	}
	
	public static SwizzleObjectIdStrategy.Transient Transient(final long startingObjectId)
	{
		return new SwizzleObjectIdStrategy.Transient(Swizzle.validateObjectId(startingObjectId));
	}
	
	public final class Transient implements SwizzleObjectIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long startingObjectId;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingObjectId)
		{
			super();
			this.startingObjectId = startingObjectId;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final long startingObjectId()
		{
			return this.startingObjectId;
		}

		@Override
		public final SwizzleObjectIdProvider createObjectIdProvider()
		{
			return SwizzleObjectIdProvider.Transient(this.startingObjectId);
		}
		
	}
	
}
