package one.microstream.storage.types;

import one.microstream.logging.types.LoggingWrapper;
import one.microstream.wrapping.Wrapper;

public interface StorageLoggingWrapper<W> extends LoggingWrapper<W, StorageLogger>
{
	@Override
	public StorageLogger logger();


	public static abstract class Abstract<W> extends Wrapper.Abstract<W> implements StorageLoggingWrapper<W>
	{
		protected Abstract(final W wrapped)
		{
			super(wrapped);
		}

		@Override
		public StorageLogger logger()
		{
			return StorageLogger.get();
		}

	}

}
