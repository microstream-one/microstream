package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.Chunks;

public interface StorageValidatorDataChunk
{
	public void validateDataChunk(Chunks[] chunks);



	public interface Provider
	{
		public StorageValidatorDataChunk provideDataChunkValidator();
	}



	public final class NoOp implements StorageValidatorDataChunk, Provider
	{
		@Override
		public final StorageValidatorDataChunk provideDataChunkValidator()
		{
			return this;
		}

		@Override
		public final void validateDataChunk(final Chunks[] byteBuffer)
		{
			// no-op
		}

	}

}
