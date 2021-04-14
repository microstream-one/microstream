package one.microstream.storage.restservice.sparkjava.exceptions;

public class InvalidRouteParametersException extends RuntimeException
{
	private static final String EXCEPTION_TEXT = "invalid url parameter ";

	public InvalidRouteParametersException(final String parameterName)
	{
		super(EXCEPTION_TEXT + parameterName);
	}

}
