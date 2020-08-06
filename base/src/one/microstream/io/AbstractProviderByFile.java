package one.microstream.io;

import static one.microstream.X.notNull;

import one.microstream.afs.AFS;
import one.microstream.afs.AFile;

public abstract class AbstractProviderByFile
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final void write(final AFile file, final String value)
	{
		AFS.writeString(file, value);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final AFile file;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractProviderByFile(final AFile file)
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
		return AFS.readString(this.file);
	}

}
