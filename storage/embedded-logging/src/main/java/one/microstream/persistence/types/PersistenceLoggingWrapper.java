package one.microstream.persistence.types;

import one.microstream.logging.types.LoggingWrapper;
import one.microstream.wrapping.Wrapper;

public interface PersistenceLoggingWrapper<W> extends LoggingWrapper<W, PersistenceLogger>
{
	@Override
	public PersistenceLogger logger();


	public static abstract class Abstract<W> extends Wrapper.Abstract<W> implements PersistenceLoggingWrapper<W>
	{
		protected Abstract(final W wrapped)
		{
			super(wrapped);
		}

		@Override
		public PersistenceLogger logger()
		{
			return PersistenceLogger.get();
		}

	}

}
