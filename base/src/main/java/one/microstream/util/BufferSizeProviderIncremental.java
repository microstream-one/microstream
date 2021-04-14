package one.microstream.util;

import one.microstream.math.XMath;
import one.microstream.typing.Immutable;
import one.microstream.typing.Stateless;


public interface BufferSizeProviderIncremental extends BufferSizeProvider
{
	public default long provideIncrementalBufferSize()
	{
		return this.provideBufferSize();
	}



	public final class Default implements BufferSizeProviderIncremental, Stateless
	{
		// since default methods, java is missing interface instantiation
	}
	
	
	public static BufferSizeProviderIncremental New()
	{
		return new BufferSizeProviderIncremental.Default();
	}
	
	public static BufferSizeProviderIncremental New(final long bufferSize)
	{
		return New(bufferSize, bufferSize);
	}
	
	public static BufferSizeProviderIncremental New(final long initialBufferSize, final long incrementalBufferSize)
	{
		return new BufferSizeProviderIncremental.Sized(
			XMath.positive(initialBufferSize),
			XMath.positive(incrementalBufferSize)
		);
	}
	
	public final class Sized implements BufferSizeProviderIncremental, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long initialBufferSize    ;
		private final long incrementalBufferSize;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Sized(final long initialBufferSize, final long incrementalBufferSize)
		{
			super();
			this.initialBufferSize     = initialBufferSize    ;
			this.incrementalBufferSize = incrementalBufferSize;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long provideBufferSize()
		{
			return this.initialBufferSize;
		}

		@Override
		public final long provideIncrementalBufferSize()
		{
			return this.incrementalBufferSize;
		}

	}

}
