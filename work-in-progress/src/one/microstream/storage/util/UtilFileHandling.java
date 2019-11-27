package one.microstream.storage.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Function;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.io.XPaths;
import one.microstream.meta.XDebug;


public class UtilFileHandling
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final Function<String, HashEnum<Path>> FILE_INDEX_SUPPLIER = f -> HashEnum.New();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static Function<String, HashEnum<Path>> fileIndexSupplier()
	{
		return FILE_INDEX_SUPPLIER;
	}
	
	public static Path mustDirectory(final Path file)
	{
		if(XPaths.isDirectoryUnchecked(file))
		{
			return file;
		}
		
		throw new RuntimeException("Not a directory: " + file);
	}
	
	public static Function<Path, String> fileIdentitySimpleNameSizeChangeTime()
	{
		return (final Path file) ->
		{
			return XPaths.getFileName(file)
				+ " $" + XPaths.sizeUnchecked(file)
				+ " @" + XPaths.lastModifiedUnchecked(file)
			;
		};
	}
	
	
	
	
	public static final void indexFiles(
		final Path                                directory     ,
		final EqHashTable<String, HashEnum<Path>> indexedFiles  ,
		final Function<Path, String>              fileIdentifier
	)
	{
//		XDebug.println("Indexing directory " + directory);
		
		final Path[] files = XPaths.listChildrenUnchecked(directory);
		for(final Path file : files)
		{
			if(XPaths.isDirectoryUnchecked(file))
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
		for(final Path file : files)
		{
			if(XPaths.isDirectoryUnchecked(file))
			{
				indexFiles(file, indexedFiles, fileIdentifier);
			}
		}
	}
	
	public static void move(
		final Path targetSourceFile,
		final Path targetTargetFile
	)
	{
		XPaths.ensureDirectoryUnchecked(targetTargetFile.getParent());
		if(XPaths.existsUnchecked(targetTargetFile))
		{
			System.out.println("x already exists: " + targetTargetFile);
			System.out.println();
			return;
		}

		System.out.println("Moving " + targetSourceFile);
		System.out.println(" to -> " + targetTargetFile);

		final Path source = targetSourceFile;
		final Path target = targetTargetFile;
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
