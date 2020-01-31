package one.microstream.storage.restservice;

@SuppressWarnings("serial")
public class InvalidRouteParametersException extends RuntimeException
{
	public InvalidRouteParametersException(final String message)
	{
		super(message);
	}

}
