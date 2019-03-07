package net.jadoth.util;

import net.jadoth.math.XMath;
import net.jadoth.memory.XMemory;
import net.jadoth.typing.Immutable;
import net.jadoth.typing.Stateless;

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
		return new BufferSizeProvider.Implementation(
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
	
	public final class Implementation implements BufferSizeProviderIncremental, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final long bufferSize;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final long bufferSize)
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
