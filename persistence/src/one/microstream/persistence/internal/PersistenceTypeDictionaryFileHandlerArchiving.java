package one.microstream.persistence.internal;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.File;
import java.text.SimpleDateFormat;

import one.microstream.concurrency.XThreads;
import one.microstream.files.XFiles;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;


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
		final String fileName = this.filePrefix + sdf.format(System.currentTimeMillis()) + this.fileSuffix;
		
		final File file = new File(this.tdArchive, fileName);
		if(file.exists())
		{
			// yes, it's weird, but it actually happened during testing. Multiple updates and moves per ms.
			XThreads.sleep(1); // crucial to prevent hundreds or even thousands of retries.
			return this.buildArchiveFile();
		}
		
		return file;
	}
	
	private void moveCurrentFileToArchive()
	{
		XFiles.ensureDirectory(this.tdArchive);
		UtilPersistenceIo.move(this.file(), this.buildArchiveFile());
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
