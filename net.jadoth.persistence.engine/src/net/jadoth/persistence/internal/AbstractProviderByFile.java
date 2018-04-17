package net.jadoth.persistence.internal;

import static net.jadoth.Jadoth.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.util.file.JadothFiles;

public abstract class AbstractProviderByFile
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final Charset standardCharset()
	{
		return StandardCharsets.UTF_8;
	}
	
	public static final void write(final File file, final String value) throws PersistenceExceptionTransfer
	{
		try
		{
			JadothFiles.writeStringToFile(file, value, standardCharset());
		}
		catch(final Exception e)
		{
			throw new PersistenceExceptionTransfer(e);
		}
	}

//	public static final long readId(final File file, final _longReference defaultId)
//	{
//		if(!file.exists())
//		{
//			return defaultId.get();
//		}
//		try
//		{
//			return Long.parseLong(JadothFiles.readStringFromFile(file, standardCharset()));
//		}
//		catch(final Exception e)
//		{
//			throw new PersistenceExceptionTransfer(e);
//		}
//	}




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
	
	protected void write(final String value) throws IOException
	{
		JadothFiles.writeStringToFile(this.file, value, standardCharset());
	}
	
	protected boolean canRead()
	{
		return this.file.exists();
	}

	protected String read() throws IOException
	{
		return JadothFiles.readStringFromFile(this.file, standardCharset());
	}

}
