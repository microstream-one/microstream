package one.microstream.experimental.checking;


public class MainTestChecker
{
//	static final Function<Throwable, RuntimeException> LOCAL_EXCEPTION_CONTEXT = new Function<Throwable, RuntimeException>()
//		new RuntimeException("Exception in test", e)
//	;


	static RuntimeException wrap(final Throwable t)
	{
		return new RuntimeException("Exception in test", t);
	}

	public static void main(final String[] args)
	{
//		final Object s = Check.notNull(provideString(), MainTestChecker::wrap);

		// (09.03.2015 TM)TODO: cut stacktrace properly (test compatibility with lambda use.)
		// (09.03.2015 TM)TODO: make static checker method instead, sufficient for most use cases.
	}


	static String provideString()
	{
//		return "a";
		return null;
	}
}
