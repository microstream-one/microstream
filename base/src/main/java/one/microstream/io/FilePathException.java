package one.microstream.io;

import java.nio.file.Path;


public class FilePathException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Path subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public FilePathException(final Path subject)
	{
		super();
		this.subject = subject;
	}

	public FilePathException(final Path subject, final String message, final Throwable cause)
	{
		super(message, cause);
		this.subject = subject;
	}

	public FilePathException(final Path subject, final String message)
	{
		super(message);
		this.subject = subject;
	}

	public FilePathException(final Path subject, final Throwable cause)
	{
		super(cause);
		this.subject = subject;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Path getSubject()
	{
		return this.subject;
	}
	
	@Override
	public String getMessage()
	{
		return super.getMessage() + " " + this.subject;
	}

}
