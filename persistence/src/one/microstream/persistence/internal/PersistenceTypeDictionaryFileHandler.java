package one.microstream.persistence.internal;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.file.Path;

import one.microstream.io.XIO;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionSource;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;

public class PersistenceTypeDictionaryFileHandler implements PersistenceTypeDictionaryIoHandler
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final String readTypeDictionary(final Path file)
	{
		return readTypeDictionary(file, null);
	}

	public static final String readTypeDictionary(final Path file, final String defaultString)
	{
		try
		{
			if(!XIO.exists(file))
			{
				return defaultString;
			}
			
			return XIO.readString(file, Persistence.standardCharset());
		}
		catch(final IOException e)
		{
			throw new PersistenceExceptionSource(e);
		}
	}

	public static final void writeTypeDictionary(final Path file, final String typeDictionaryString)
	{
		try
		{
			XIO.write(file, typeDictionaryString, Persistence.standardCharset());
		}
		catch(final Exception t)
		{
			throw new PersistenceException(t);
		}
	}
	
	// sadly, the JDK geniuses didn't have enough OOP skill to implement proper FSElement types like Directory and File.
	public static PersistenceTypeDictionaryFileHandler NewInDirectory(final Path directory)
	{
		return NewInDirectory(directory, null);
	}
	
	public static PersistenceTypeDictionaryFileHandler New(final Path file)
	{
		return New(file, null);
	}
	
	public static PersistenceTypeDictionaryFileHandler NewInDirectory(
		final Path                            directory    ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		return new PersistenceTypeDictionaryFileHandler(
			XIO.Path(directory, Persistence.defaultFilenameTypeDictionary()),
			mayNull(writeListener)
		);
	}
	
	public static PersistenceTypeDictionaryFileHandler New(
		final Path                            file         ,
		final PersistenceTypeDictionaryStorer writeListener)
	{
		return new PersistenceTypeDictionaryFileHandler(
			notNull(file)         ,
			mayNull(writeListener)
		);
	}

	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Path                            file         ;
	private final PersistenceTypeDictionaryStorer writeListener;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceTypeDictionaryFileHandler(
		final Path                            file         ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		super();
		this.file          = file         ;
		this.writeListener = writeListener;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	protected Path file()
	{
		return this.file;
	}

	@Override
	public final synchronized String loadTypeDictionary()
	{
		return readTypeDictionary(this.file);
	}
	
	protected synchronized void writeTypeDictionary(final String typeDictionaryString)
	{
		writeTypeDictionary(this.file, typeDictionaryString);
	}

	@Override
	public final synchronized void storeTypeDictionary(final String typeDictionaryString)
	{
		this.writeTypeDictionary(typeDictionaryString);
		if(this.writeListener != null)
		{
			this.writeListener.storeTypeDictionary(typeDictionaryString);
		}
	}
	
	
	@FunctionalInterface
	public interface Creator
	{
		public PersistenceTypeDictionaryIoHandler createTypeDictionaryIoHandler(
			Path                            file         ,
			PersistenceTypeDictionaryStorer writeListener
		);
		
	}
	
	
	public static PersistenceTypeDictionaryFileHandler.Provider ProviderInDirectory(final Path directory)
	{
		return new PersistenceTypeDictionaryFileHandler.Provider(
			XIO.Path(directory, Persistence.defaultFilenameTypeDictionary())
		);
	}
	
	public static PersistenceTypeDictionaryFileHandler.Provider Provider(final Path file)
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
		
		private final Path file;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Provider(final Path file)
		{
			super();
			this.file = file;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDictionaryFileHandler provideTypeDictionaryIoHandler(
			final PersistenceTypeDictionaryStorer writeListener
		)
		{
			return PersistenceTypeDictionaryFileHandler.New(this.file);
		}
		
	}

}
