package one.microstream.exceptions;

public interface ExceptionCreator<E extends Exception>
{
	public E createException(String message, Throwable cause);

}
