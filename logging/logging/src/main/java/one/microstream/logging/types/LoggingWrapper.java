package one.microstream.logging.types;

import one.microstream.wrapping.Wrapper;

public interface LoggingWrapper<W, L> extends Wrapper<W>
{
	public L logger();


	public static abstract class Abstract<W, L> extends Wrapper.Abstract<W> implements LoggingWrapper<W, L>
	{
		protected Abstract(final W wrapped)
		{
			super(wrapped);
		}

	}

}
