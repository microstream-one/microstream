package one.microstream.storage.util;

import java.io.File;
import java.util.function.Function;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;

public class FileMoveSyncher
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final Function<String, HashEnum<File>> FILE_INDEX_SUPPLIER = f -> HashEnum.New();
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static File mustDirectory(final File file)
	{
		if(file.isDirectory())
		{
			return file;
		}
		
		throw new RuntimeException("Not a directory: " + file);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Function<File, String> fileIdentifier;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public FileMoveSyncher(final Function<File, String> fileIdentifier)
	{
		super();
		this.fileIdentifier = fileIdentifier;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public void moveSynch(final File sourceDirectory, final File targetDirectory)
	{
		mustDirectory(sourceDirectory);
		mustDirectory(targetDirectory);
		
		final EqHashTable<String, HashEnum<File>> indexedFiles = EqHashTable.New();
		indexFiles(targetDirectory, indexedFiles, this.fileIdentifier);
		
		final String sourceDirectoryBase = sourceDirectory.getAbsolutePath();
		synchMoveFiles(sourceDirectoryBase, sourceDirectory, targetDirectory, indexedFiles, this.fileIdentifier);
	}
	
	static final void indexFiles(
		final File                                directory     ,
		final EqHashTable<String, HashEnum<File>> indexFiles    ,
		final Function<File, String>              fileIdentifier
	)
	{
		final File[] files = directory.listFiles();
		for(final File file : files)
		{
			if(!file.isDirectory())
			{
				continue;
			}
			
			final String fileIdentity = fileIdentifier.apply(file);
			indexFiles.ensure(fileIdentity, FILE_INDEX_SUPPLIER).add(file);
		}
		for(final File file : files)
		{
			if(file.isDirectory())
			{
				indexFiles(file, indexFiles, fileIdentifier);
			}
		}
	}
	
	static final void synchMoveFiles(
		final String                              sourceDirectoryBase,
		final File                                sourceDirectory    ,
		final File                                targetDirectoryBase,
		final EqHashTable<String, HashEnum<File>> indexFiles     ,
		final Function<File, String>              fileIdentifier
	)
	{
		final File[] sourceFiles = sourceDirectory.listFiles();
		
		for(final File sourceFile : sourceFiles)
		{
			if(sourceFile.isDirectory())
			{
				continue;
			}
			
			final String sourceFileIdentity = fileIdentifier.apply(sourceFile);
			final HashEnum<File> matchingTargetFiles = indexFiles.get(sourceFileIdentity);
			//  - select most suited file (already fitting path, otherwise naively first file)
			//  - move selected file to fitting path (or no-op)
			//  - handle rest (warn / delete, maybe functional)
		}
		
		for(final File sourceFile : sourceFiles)
		{
			if(!sourceFile.isDirectory())
			{
				continue;
			}
			
			final String sourceFilePath = sourceFile.getAbsolutePath();
			final File targetDirectory = new File(
				targetDirectoryBase,
				sourceFilePath.substring(sourceDirectoryBase.length())
			);
			
			synchMoveFiles(sourceDirectoryBase, sourceFile, targetDirectory, indexFiles, fileIdentifier);
		}
	}
	
}
