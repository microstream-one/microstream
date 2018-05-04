package net.jadoth.persistence.types;

import net.jadoth.memory.Memory;
import net.jadoth.typing.Immutable;
import net.jadoth.typing.Stateless;


public interface BufferSizeProvider
{
	public long initialBufferSize();

	public long incrementalBufferSize();



	public final class Default implements BufferSizeProvider, Stateless
	{

		@Override
		public final long initialBufferSize()
		{
			return Memory.defaultBufferSize();
		}

		@Override
		public final long incrementalBufferSize()
		{
			return Memory.defaultBufferSize();
		}

	}
	
	

	public final class Simple implements BufferSizeProvider, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final long bufferSize;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Simple(final long bufferSize)
		{
			super();
			this.bufferSize = bufferSize;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long initialBufferSize()
		{
			return this.bufferSize;
		}

		@Override
		public final long incrementalBufferSize()
		{
			return this.bufferSize;
		}

	}



	public final class Implementation implements BufferSizeProvider, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final long initialBufferSize    ;
		private final long incrementalBufferSize;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final long initialBufferSize, final long incrementalBufferSize)
		{
			super();
			this.initialBufferSize     = initialBufferSize    ;
			this.incrementalBufferSize = incrementalBufferSize;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long initialBufferSize()
		{
			return this.initialBufferSize;
		}

		@Override
		public final long incrementalBufferSize()
		{
			return this.incrementalBufferSize;
		}

	}

}
