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



	public void moveSynch(final File sourceDirectory, final File targetDirectory)
	{
		mustDirectory(sourceDirectory);
		mustDirectory(targetDirectory);
		
		final EqHashTable<String, HashEnum<File>> indexedFiles = EqHashTable.New();
		indexFiles(targetDirectory, indexedFiles, this.fileIdentifier);
		
		// - iterate source directory, for each non-directory:
		//  - lookup index files for each file
		//  - select most suited file (fitting path, otherwise naively first)
		//  - move selected file to fitting path (or no-op)
		//  - handle rest (warn / delete, maybe functional)
		// - for each directory: recurse
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
	
	
}
