package one.microstream.storage.types;

import one.microstream.math.XMath;

@FunctionalInterface
public interface StorageInitialDataFileNumberProvider
{
	public int provideInitialDataFileNumber(int channelIndex);
	
	
	
	public final class Implementation implements StorageInitialDataFileNumberProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int constantInitialFileNumber;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Implementation(final int constantInitialFileNumber)
		{
			super();
			this.constantInitialFileNumber = XMath.notNegative(constantInitialFileNumber);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public int provideInitialDataFileNumber(final int channelIndex)
		{
			return this.constantInitialFileNumber;
		}
		
	}
	
}
