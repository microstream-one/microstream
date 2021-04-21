package one.microstream.logging.zero.types;

import one.microstream.logging.types.LoggerFactory;
import one.microstream.logging.types.LoggerFactoryProvider;

public class ZeroLoggerFactoryProvider implements LoggerFactoryProvider
{
	public ZeroLoggerFactoryProvider()
	{
		super();
	}

	@Override
	public LoggerFactory provideLoggerFactory()
	{
		return new ZeroLoggerFactory.Default();
	}
}
