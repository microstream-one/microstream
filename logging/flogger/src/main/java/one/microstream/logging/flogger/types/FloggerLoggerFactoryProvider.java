package one.microstream.logging.flogger.types;

import one.microstream.logging.types.LoggerFactory;
import one.microstream.logging.types.LoggerFactoryProvider;

public class FloggerLoggerFactoryProvider implements LoggerFactoryProvider
{
	public FloggerLoggerFactoryProvider()
	{
		super();
	}

	@Override
	public LoggerFactory provideLoggerFactory()
	{
		return new FloggerLoggerFactory.Default();
	}
}
