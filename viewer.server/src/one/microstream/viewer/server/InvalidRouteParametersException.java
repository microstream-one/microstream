package one.microstream.viewer.server;

@SuppressWarnings("serial")
public class InvalidRouteParametersException extends RuntimeException
{
	public InvalidRouteParametersException(final String message)
	{
		super(message);
	}

}
