package one.microstream.storage.types;

import one.microstream.persistence.binary.types.Binary;

public interface StorageValidatorDataChunk
{
	public void validateDataChunk(Binary data);



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
		public final void validateDataChunk(final Binary data)
		{
			// no-op
		}

	}

}
