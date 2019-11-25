package one.microstream.storage.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Function;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.io.XFiles;
import one.microstream.meta.XDebug;


public class UtilFileHandling
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final Function<String, HashEnum<File>> FILE_INDEX_SUPPLIER = f -> HashEnum.New();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static Function<String, HashEnum<File>> fileIndexSupplier()
	{
		return FILE_INDEX_SUPPLIER;
	}
	
	public static File mustDirectory(final File file)
	{
		if(file.isDirectory())
		{
			return file;
		}
		
		throw new RuntimeException("Not a directory: " + file);
	}
	
	public static Function<File, String> fileIdentitySimpleNameSizeChangeTime()
	{
		return (final File file) ->
		{
			return file.getName() + " $" + file.length() + " @" + file.lastModified();
		};
	}
	
	
	
	
	public static final void indexFiles(
		final File                                directory     ,
		final EqHashTable<String, HashEnum<File>> indexedFiles  ,
		final Function<File, String>              fileIdentifier
	)
	{
//		XDebug.println("Indexing directory " + directory);
		
		final File[] files = directory.listFiles();
		for(final File file : files)
		{
			if(file.isDirectory())
			{
				continue;
			}
			
			final String fileIdentity = fileIdentifier.apply(file);
			indexedFiles.ensure(fileIdentity, FILE_INDEX_SUPPLIER).add(file);
			if(indexedFiles.size() % 1000 == 0)
			{
				XDebug.println(indexedFiles.size() + " files processed.");
			}
		}
		for(final File file : files)
		{
			if(file.isDirectory())
			{
				indexFiles(file, indexedFiles, fileIdentifier);
			}
		}
	}
	
	public static void move(
		final File targetSourceFile,
		final File targetTargetFile
	)
	{
		XFiles.ensureDirectory(targetTargetFile.getParentFile());
		if(targetTargetFile.exists())
		{
			System.out.println("x already exists: " + targetTargetFile);
			System.out.println();
			return;
		}

		System.out.println("Moving " + targetSourceFile);
		System.out.println(" to -> " + targetTargetFile);

		final Path source = targetSourceFile.toPath();
		final Path target = targetTargetFile.toPath();
		try
		{
			Files.move(source, target);
		}
		catch(final NoSuchFileException e)
		{
			System.out.println("No such file: " + e.getMessage());
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private UtilFileHandling()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
