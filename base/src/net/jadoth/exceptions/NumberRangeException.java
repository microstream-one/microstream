/**
 *
 */
package net.jadoth.exceptions;

/**
 * @author Thomas Muenz
 *
 */
public class NumberRangeException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public NumberRangeException()
	{
		super();
	}

	public NumberRangeException(final String message)
	{
		super(message);
	}

	public NumberRangeException(final Throwable cause)
	{
		super(cause);
	}

	public NumberRangeException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
