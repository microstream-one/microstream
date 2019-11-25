package one.microstream.io;

import java.io.File;


public class DirectoryException extends FileException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public DirectoryException(final File subject)
	{
		super(subject);
	}

	public DirectoryException(final File subject, final String message, final Throwable cause)
	{
		super(subject, message, cause);
	}

	public DirectoryException(final File subject, final String message)
	{
		super(subject, message);
	}

	public DirectoryException(final File subject, final Throwable cause)
	{
		super(subject, cause);
	}
	
}
