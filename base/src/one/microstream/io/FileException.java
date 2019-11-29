package one.microstream.io;

import java.io.File;


public class FileException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final File subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public FileException(final File subject)
	{
		super();
		this.subject = subject;
	}

	public FileException(final File subject, final String message, final Throwable cause)
	{
		super(message, cause);
		this.subject = subject;
	}

	public FileException(final File subject, final String message)
	{
		super(message);
		this.subject = subject;
	}

	public FileException(final File subject, final Throwable cause)
	{
		super(cause);
		this.subject = subject;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public File getSubject()
	{
		return this.subject;
	}
	
	@Override
	public String getMessage()
	{
		return super.getMessage() + " " + this.subject;
	}

}
