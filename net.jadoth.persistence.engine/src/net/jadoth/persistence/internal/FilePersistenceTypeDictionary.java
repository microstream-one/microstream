package net.jadoth.persistence.internal;

import static net.jadoth.Jadoth.notNull;

import java.io.File;
import java.io.IOException;

import net.jadoth.persistence.exceptions.PersistenceException;
import net.jadoth.persistence.exceptions.PersistenceExceptionSource;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceTypeDictionaryLoader;
import net.jadoth.persistence.types.PersistenceTypeDictionaryStorer;
import net.jadoth.util.file.JadothFiles;

public final class FilePersistenceTypeDictionary
implements PersistenceTypeDictionaryLoader, PersistenceTypeDictionaryStorer
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final String readTypeDictionary(final File file)
	{
		return readTypeDictionary(file, null);
	}

	public static final String readTypeDictionary(final File file, final String defaultString)
	{
		if(!file.exists())
		{
			return defaultString;
		}
		try
		{
			return JadothFiles.readStringFromFile(file, Persistence.dictionaryCharset());
		}
		catch(final IOException e)
		{
			throw new PersistenceExceptionSource(e);
		}
	}

	public static final void writeTypeDictionary(final File file, final String typeDictionaryString)
	{
		try
		{
			JadothFiles.writeStringToFile(file, typeDictionaryString, Persistence.dictionaryCharset());
		}
		catch(final Exception t)
		{
			throw new PersistenceException(t);
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final File file;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public FilePersistenceTypeDictionary(final File file)
	{
		super();
		this.file = notNull(file);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final synchronized String loadTypeDictionary()
	{
		return readTypeDictionary(this.file);
	}

	@Override
	public final synchronized void storeTypeDictionary(final String typeDictionaryString)
	{
		writeTypeDictionary(this.file, typeDictionaryString);
	}

}
