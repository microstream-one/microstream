package one.microstream.files;

import static one.microstream.X.notNull;

import java.io.File;

import one.microstream.chars.XChars;
import one.microstream.io.XIO;

public abstract class AbstractProviderByFile
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final void write(final File file, final String value)
	{
		XIO.execute(() ->
		{
			XFiles.writeStringToFile(file, value, XChars.standardCharset());
			
			return null;
		});
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final File file;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractProviderByFile(final File file)
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
		return this.file.exists();
	}

	protected String read()
	{
		return XIO.execute(() ->
			XFiles.readStringFromFile(this.file.toPath())
		);
	}

}
