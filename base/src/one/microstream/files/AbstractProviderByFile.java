package one.microstream.files;

import static one.microstream.X.notNull;

import java.io.File;

public abstract class AbstractProviderByFile
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final void write(final File file, final String value)
	{
		XFiles.writeStringToFile(file, value, XFiles.standardCharset(), RuntimeException::new);
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
		return XFiles.readStringFromFile(this.file, XFiles.standardCharset(), RuntimeException::new);
	}

}
