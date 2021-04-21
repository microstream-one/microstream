package one.microstream.logging.types;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.ServiceLoader;

import one.microstream.chars.XChars;

public interface LoggerFactory
{
	public default Logger forClass(final Class<?> clazz)
	{
		return this.forName(clazz.getName());
	}

	public Logger forName(String name);


	public static LoggerFactory set(final LoggerFactory globalLoggerFactory)
	{
		return Static.set(globalLoggerFactory);
	}

	public static LoggerFactory get()
	{
		return Static.get();
	}


	public static class Static
	{
		private static LoggerFactory globalLoggerFactory;

		static synchronized LoggerFactory set(final LoggerFactory globalLoggerFactory)
		{
			final LoggerFactory old = Static.globalLoggerFactory;
			Static.globalLoggerFactory = globalLoggerFactory;
			return old;
		}

		static synchronized LoggerFactory get()
		{
			if(globalLoggerFactory == null)
			{
				globalLoggerFactory = load();
			}

			return globalLoggerFactory;
		}

		private static LoggerFactory load()
		{
			final Iterator<LoggerFactoryProvider> iterator = ServiceLoader
				.load(LoggerFactoryProvider.class)
				.iterator();
			if(iterator.hasNext())
			{
				return iterator.next().provideLoggerFactory();
			}

			throw new IllegalStateException("No logging service provided");
		}

		static
		{
			if(!"false".equalsIgnoreCase(System.getProperty("microstream.log.banner")))
			{
				try(InputStream inputStream = LoggerFactory.class.getResourceAsStream("banner.txt"))
				{
					final String banner = XChars.readStringFromInputStream(inputStream, StandardCharsets.UTF_8);
					get().forClass(LoggerFactory.class).info().log(banner);
				}
				catch(final Exception e)
				{
					// swallow
				}
			}
		}

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		* Dummy constructor to prevent instantiation of this static-only utility class.
		*
		* @throws UnsupportedOperationException
		*/
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}

	}

}
