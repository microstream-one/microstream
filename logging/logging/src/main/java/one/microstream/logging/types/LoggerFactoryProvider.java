package one.microstream.logging.types;

@FunctionalInterface
public interface LoggerFactoryProvider
{
	public LoggerFactory provideLoggerFactory();
}
