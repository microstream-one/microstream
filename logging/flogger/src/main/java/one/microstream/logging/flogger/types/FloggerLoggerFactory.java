package one.microstream.logging.flogger.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.backend.LoggerBackend;
import com.google.common.flogger.backend.Platform;

import one.microstream.logging.types.Logger;
import one.microstream.logging.types.LoggerFactory;

public interface FloggerLoggerFactory extends LoggerFactory
{
	public static class Default implements FloggerLoggerFactory
	{
		Default()
		{
			super();
		}

		@Override
		public Logger forName(final String name)
		{
			final LoggerBackend backend = Platform.getBackend(name);
			try
			{
				final Constructor<FluentLogger> constructor = FluentLogger.class
					.getDeclaredConstructor(LoggerBackend.class)
				;
				constructor.setAccessible(true);
				final FluentLogger flogger = constructor.newInstance(backend);
				return new FloggerLogger.Default(flogger);
			}
			catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
