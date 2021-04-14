
package one.microstream.cache.types;

import javax.cache.processor.EntryProcessorException;


public interface EntryProcessorResult<T> extends javax.cache.processor.EntryProcessorResult<T>
{
	public static <T> EntryProcessorResult<T> New(final T value)
	{
		return () -> value;
	}
	
	public static <T> EntryProcessorResult<T> New(final Exception e)
	{
		final EntryProcessorException epe = e instanceof EntryProcessorException
			? (EntryProcessorException)e
			: new EntryProcessorException(e);
		return () -> {
			throw epe;
		};
	}
}
