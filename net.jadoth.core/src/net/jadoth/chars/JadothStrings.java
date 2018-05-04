package net.jadoth.chars;

public class JadothStrings
{
	
	
	
	private JadothStrings()
	{
		// static only
		throw new UnsupportedOperationException();
	}

	/**
	 * Utility method that replicates the JVM's intrinsic system string as defined in {@link Object#toString()}.
	 * (It's funny how much functionality is missing in the JDK API).
	 * 
	 * @param instance the instance whose system string shall be generated.
	 * @return the system string for the passed instance.
	 */
	/**
	 * @param instance
	 * @return
	 */
	public static String systemString(final Object instance)
	{
		return instance == null
			? null
			: instance.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(instance))
		;
	}

	public static String nonNullString(final Object object)
	{
		return object == null ? "" : object.toString();
	}

	/**
	 * Returns {@code value.toString()} if the passed value is not {@literal null}, otherwise {@literal null}.
	 * <p>
	 * Note that this is a different behavior than {@link String#valueOf(Object)} has, as the latter returns
	 * the string {@code "null"} for a passed {@literal null} reference. The latter is merely an output helper
	 * method, albeit clumsily named and placed as if it were a general utility method. THIS method here
	 * is the far more useful general utility method.
	 * <p>
	 * The behavior of this method is needed for example for converting values in a generic data structure
	 * (e.g. a Object[] array) to string values but have the actual values, including {@literal null}
	 *  (information about a missing value), maintained.
	 *
	 * @param value the value to be projected to its string representation if not null.
	 * @return a string representation of an actual passed value or a transient {@literal null}.
	 *
	 * @see Object#toString()
	 * @see String#valueOf(Object)
	 */
	public static String valueString(final Object value)
	{
		return value == null ? null : value.toString();
	}
	
}
