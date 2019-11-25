package one.microstream.io;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.file.Path;

import one.microstream.exceptions.IORuntimeException;

public abstract class AbstractProviderByFile
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final void write(final Path file, final String value)
	{
		XIO.execute(() ->
		{
			XPaths.write(file, value);
			
			return null;
		});
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Path file;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractProviderByFile(final Path file)
	{
		super();
		this.file = notNull(file);
	}
	
	protected void write(final String value)
	{
		write(this.file, value);
	}
	
	protected boolean canRead()
	{
		try
		{
			return XPaths.exists(this.file);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}

	protected String read()
	{
		return XIO.execute(() ->
			XPaths.readString(this.file)
		);
	}

}
