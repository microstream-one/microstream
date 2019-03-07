/**
 * 
 */
package net.jadoth.math;

/**
 * @author Thomas Muenz
 *
 */
public class InvalidCoordinateException extends RuntimeException
{
	/**
	 * 
	 */

	
	
	/**
	 * 
	 */
	public InvalidCoordinateException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public InvalidCoordinateException(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidCoordinateException(final Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidCoordinateException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
