package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.files.XFiles;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.Persistence;

public abstract class AbstractProviderByFile
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final void write(final File file, final String value) throws PersistenceExceptionTransfer
	{
		XFiles.writeStringToFile(file, value, Persistence.standardCharset(), PersistenceExceptionTransfer::new);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final File file;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
		return XFiles.readStringFromFile(this.file, Persistence.standardCharset(), PersistenceExceptionTransfer::new);
	}

}
