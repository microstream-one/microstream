package one.microstream.persistence.internal;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.text.SimpleDateFormat;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AWritableFile;
import one.microstream.concurrency.XThreads;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;


public class PersistenceTypeDictionaryFileHandlerArchiving extends PersistenceTypeDictionaryFileHandler
{
	public static PersistenceTypeDictionaryFileHandlerArchiving New(
		final AFile                           file         ,
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
	
	private final ADirectory directory ;
	private final ADirectory tdArchive ;
	private final String filePrefix;
	private final String fileSuffix;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceTypeDictionaryFileHandlerArchiving(
		final AFile                           file         ,
		final PersistenceTypeDictionaryStorer writeListener
	)
	{
		super(file, writeListener);
		this.directory  = file.parent();
		
		// (28.07.2020 TM)TODO: magic value String
		this.tdArchive  = this.directory.ensureDirectory("TypeDictionaryArchive");
		this.filePrefix = file.name();
		this.fileSuffix = file.type();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private AFile buildArchiveFile()
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss_SSS");
		final String fileName = this.filePrefix + sdf.format(System.currentTimeMillis()) + this.fileSuffix;
		
		final AFile file = this.tdArchive.ensureFile(fileName);
		
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
		this.tdArchive.ensureExists();
		
		final AFile targetFile = this.buildArchiveFile();
		
		final AWritableFile wSourceFile = this.file().useWriting();
		try
		{
			final AWritableFile wTargetFile = targetFile.useWriting();
			try
			{
				wSourceFile.moveTo(wTargetFile);
			}
			finally
			{
				wTargetFile.release();
			}
		}
		finally
		{
			wSourceFile.release();
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
