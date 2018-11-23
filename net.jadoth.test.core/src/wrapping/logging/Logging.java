package wrapping.logging;

import wrapping.MyType;

// Optional. Nur als Beispiel.
public class Logging
{
	public static MyType addTo(final MyType instance)
	{
		return new MyTypeLoggingWrapperCool(instance);
	}
}
