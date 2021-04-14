package one.microstream.util;

import one.microstream.math.XMath;
import one.microstream.memory.XMemory;
import one.microstream.typing.Immutable;
import one.microstream.typing.Stateless;

public interface BufferSizeProvider
{
	public default long provideBufferSize()
	{
		return XMemory.defaultBufferSize();
	}
	
	public static BufferSizeProvider New()
	{
		return new BufferSizeProvider.Default();
	}
	
	public static BufferSizeProvider New(final long bufferSize)
	{
		return new BufferSizeProvider.Sized(
			XMath.positive(bufferSize)
		);
	}
	
	public final class Default implements BufferSizeProvider, Stateless
	{
		Default()
		{
			super();
		}
	}
	
	public final class Sized implements BufferSizeProviderIncremental, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long bufferSize;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Sized(final long bufferSize)
		{
			super();
			this.bufferSize = bufferSize;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long provideBufferSize()
		{
			return this.bufferSize;
		}

		@Override
		public final long provideIncrementalBufferSize()
		{
			return this.bufferSize;
		}

	}
	
}
