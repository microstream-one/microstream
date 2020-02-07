package storage.restservice.sparkjava;

@SuppressWarnings("serial")
public class InvalidRouteParametersException extends RuntimeException
{
	public InvalidRouteParametersException(final String message)
	{
		super(message);
	}

}
