package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import java.io.File;
import java.io.IOException;

import net.jadoth.files.XFiles;
import net.jadoth.persistence.exceptions.PersistenceException;
import net.jadoth.persistence.exceptions.PersistenceExceptionSource;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceTypeDictionaryIoHandler;

public final class PersistenceTypeDictionaryFileHandler implements PersistenceTypeDictionaryIoHandler
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
			return XFiles.readStringFromFile(file, Persistence.standardCharset());
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
			XFiles.writeStringToFile(file, typeDictionaryString, Persistence.standardCharset());
		}
		catch(final Exception t)
		{
			throw new PersistenceException(t);
		}
	}
	
	// sadly, the JDK geniuses didn't have enough OOP skill to implement proper FSElement types like Directory and File.
	public static PersistenceTypeDictionaryFileHandler NewInDirecoty(final File directory)
	{
		return new PersistenceTypeDictionaryFileHandler(
			new File(directory, Persistence.defaultFilenameTypeDictionary())
		);
	}
	
	public static PersistenceTypeDictionaryFileHandler New(final File file)
	{
		return new PersistenceTypeDictionaryFileHandler(
			notNull(file)
		);
	}

	

	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final File file;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	PersistenceTypeDictionaryFileHandler(final File file)
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
	
	
	
	public static PersistenceTypeDictionaryFileHandler.Provider ProviderInDirecory(final File directory)
	{
		return new PersistenceTypeDictionaryFileHandler.Provider(
			new File(directory, Persistence.defaultFilenameTypeDictionary())
		);
	}
	
	public static PersistenceTypeDictionaryFileHandler.Provider Provider(final File file)
	{
		return new PersistenceTypeDictionaryFileHandler.Provider(
			notNull(file)
		);
	}
	
	public static final class Provider implements PersistenceTypeDictionaryIoHandler.Provider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final File file;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Provider(final File file)
		{
			super();
			this.file = file;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDictionaryFileHandler provideTypeDictionaryIoHandler()
		{
			return PersistenceTypeDictionaryFileHandler.New(this.file);
		}
		
	}

}
