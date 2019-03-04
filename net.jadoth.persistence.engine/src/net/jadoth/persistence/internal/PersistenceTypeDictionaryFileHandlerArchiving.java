package net.jadoth.persistence.internal;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;

import net.jadoth.files.XFiles;
import net.jadoth.persistence.exceptions.PersistenceException;
import net.jadoth.persistence.types.PersistenceTypeDictionaryStorer;


public class PersistenceTypeDictionaryFileHandlerArchiving extends PersistenceTypeDictionaryFileHandler
{
	public static PersistenceTypeDictionaryFileHandlerArchiving New(
		final File                            file         ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		return new PersistenceTypeDictionaryFileHandlerArchiving(
			notNull(file)         ,
			mayNull(writeListener)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final File   directory ;
	private final File   tdArchive ;
	private final String filePrefix;
	private final String fileSuffix;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceTypeDictionaryFileHandlerArchiving(
		final File                            file         ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		super(file, writeListener);
		this.directory = file.getParentFile();
		this.tdArchive = new File(this.directory, "TypeDictionaryArchive");
		
		final String fileName = file.getName();
		final int dotIndex = fileName.lastIndexOf('.');
		
		if(dotIndex < 0)
		{
			this.filePrefix = fileName;
			this.fileSuffix = "";
		}
		else
		{
			this.filePrefix = fileName.substring(0, dotIndex);
			this.fileSuffix = fileName.substring(dotIndex);
		}
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private File buildArchiveFile()
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss_SSS");
		
		return new File(this.tdArchive, this.filePrefix + sdf.format(System.currentTimeMillis()) + this.fileSuffix);
	}
	
	private void moveCurrentFileToArchive()
	{
		XFiles.ensureDirectory(this.tdArchive);
		
		final Path source = this.file().toPath();
		final Path target = this.buildArchiveFile().toPath();
		
		try
		{
			Files.move(source, target);
		}
		catch(final Exception e)
		{
			throw new PersistenceException("Could not move type dictionary file to " + target, e);
		}
	}
	
	@Override
	protected synchronized void writeTypeDictionary(final String typeDictionaryString)
	{
		// there is no file to be moved on the first call.
		if(this.file().exists())
		{
			this.moveCurrentFileToArchive();
		}
		
		super.writeTypeDictionary(typeDictionaryString);
	}
	
}
