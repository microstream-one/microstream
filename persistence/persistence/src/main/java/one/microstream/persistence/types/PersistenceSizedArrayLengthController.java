package one.microstream.persistence.types;

import one.microstream.math.XMath;

public interface PersistenceSizedArrayLengthController
{
	public int controlArrayLength(int specifiedCapacity, int actualElementCount);
	
	
	
	/**
	 * Recommended for storing data (does not change program behavior).
	 * 
	 */
	public static PersistenceSizedArrayLengthController.Unrestricted Unrestricted()
	{
		return new PersistenceSizedArrayLengthController.Unrestricted();
	}
	
	/**
	 * Recommended for communication (prevents array bombs).
	 * 
	 */
	public static PersistenceSizedArrayLengthController.Fitting Fitting()
	{
		return new PersistenceSizedArrayLengthController.Fitting();
	}
	
	public static PersistenceSizedArrayLengthController.Limited Limited(final int maximumCapacity)
	{
		return new PersistenceSizedArrayLengthController.Limited(
			XMath.positive(maximumCapacity)
		);
	}
	
	public final class Unrestricted implements PersistenceSizedArrayLengthController
	{
		Unrestricted()
		{
			super();
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return specifiedCapacity;
		}
		
	}
	
	public final class Fitting implements PersistenceSizedArrayLengthController
	{
		Fitting()
		{
			super();
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return actualElementCount;
		}
		
	}
	
	public final class Limited implements PersistenceSizedArrayLengthController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int limit;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Limited(final int limit)
		{
			super();
			this.limit = limit;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final int limit()
		{
			return this.limit;
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return Math.min(specifiedCapacity, Math.max(this.limit, actualElementCount));
		}
		
	}
	
}
