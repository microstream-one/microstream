package net.jadoth.swizzling.types;

public interface SwizzleObjectIdProvider extends SwizzleObjectIdLookup
{
	public long provideNextObjectId();

	public SwizzleObjectIdProvider initializeObjectId();

	public SwizzleObjectIdProvider updateCurrentObjectId(long currentObjectId);

	
	
	public static SwizzleObjectIdProvider Transient()
	{
		return new Transient(Swizzle.defaultStartObjectId());
	}
	
	public static SwizzleObjectIdProvider Transient(final long startingObjectId)
	{
		return new Transient(Swizzle.validateObjectId(startingObjectId));
	}
	
	public final class Transient implements SwizzleObjectIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private long currentObjectId;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Transient(final long startingObjectId)
		{
			super();
			this.currentObjectId = startingObjectId;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final synchronized long provideNextObjectId()
		{
			return ++this.currentObjectId;
		}

		@Override
		public final synchronized long currentObjectId()
		{
			return this.currentObjectId;
		}

		@Override
		public final Transient initializeObjectId()
		{
			return this;
		}

		@Override
		public final synchronized SwizzleObjectIdProvider updateCurrentObjectId(final long currentObjectId)
		{
			this.currentObjectId = currentObjectId;
			return this;
		}

	}
	
}
