package one.microstream.logging.zero.types;

import com.obsidiandynamics.zerolog.Zlg;

import one.microstream.logging.types.Logger;
import one.microstream.logging.types.LoggerFactory;

public interface ZeroLoggerFactory extends LoggerFactory
{
	public static class Default implements ZeroLoggerFactory
	{
		Default()
		{
			super();
		}

		@Override
		public Logger forName(final String name)
		{
			return new ZeroLogger.Default(Zlg.forName(name).get());
		}
	}
}
