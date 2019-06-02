package one.microstream.storage.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.files.XFiles;
import one.microstream.meta.XDebug;
import one.microstream.typing.KeyValue;


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
	
	public static Function<File, String> fileIdentitySimpleNameSizeChangeTime()
	{
		return (final File file) ->
		{
			return file.getName() + " $" + file.length() + " @" + file.lastModified();
		};
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
		
		XDebug.println("Indexing files ...");
		indexFiles(targetDirectory, indexedFiles, this.fileIdentifier);
		XDebug.println("Indexed unique files: " + indexedFiles.size());
		
//		checkForDuplicates(indexedFiles);
		
		XDebug.println("Synching files ...");
		final String sourceDirectoryBase = sourceDirectory.getAbsolutePath();
		synchMoveDirectoryContent(
			sourceDirectoryBase,
			sourceDirectory,
			targetDirectory,
			sourceDirectoryBase.length(),
			targetDirectory.getAbsolutePath().length(),
			indexedFiles,
			this.fileIdentifier
		);
	}
	
	static final void indexFiles(
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
	
	static final void checkForDuplicates(
		final EqHashTable<String, HashEnum<File>> indexedFiles
	)
	{
		for(final KeyValue<String, HashEnum<File>> e : indexedFiles)
		{
			if(e.value().size() > 1)
			{
				System.out.println("Duplicates:");
				e.value().iterate(System.out::println);
				System.out.println();
			}
		}
	}
	
	private static String getRelativePath(final File f, final int directoryBaseLength)
	{
		return f.getAbsolutePath().substring(directoryBaseLength);
	}
	
	static final void synchMoveDirectoryContent(
		final String                              srcDirBase     ,
		final File                                sourceDirectory,
		final File                                trgDirBase     ,
		final int                                 srcBaseLength  ,
		final int                                 trgBaseLength  ,
		final EqHashTable<String, HashEnum<File>> indexFiles     ,
		final Function<File, String>              fileIdentifier
	)
	{
		final File[] sourceFiles = sourceDirectory.listFiles();
		final String relativeSourcePath = getRelativePath(sourceDirectory, srcBaseLength);
		synchMoveRealFiles(srcDirBase, trgDirBase, srcBaseLength, trgBaseLength, relativeSourcePath, sourceFiles, indexFiles, fileIdentifier);
		synchMoveSubDirs(srcDirBase, trgDirBase, srcBaseLength, trgBaseLength, sourceFiles, indexFiles, fileIdentifier);
	}
	
	static final void synchMoveRealFiles(
		final String                              sourceDirectoryBase,
		final File                                targetDirectoryBase,
		final int                                 srcBaseLength      ,
		final int                                 trgBaseLength      ,
		final String                              relativeSourcePath ,
		final File[]                              sourceFiles        ,
		final EqHashTable<String, HashEnum<File>> indexFiles         ,
		final Function<File, String>              fileIdentifier
	)
	{
		for(final File sourceFile : sourceFiles)
		{
			if(sourceFile.isDirectory())
			{
				continue;
			}
			
			final String  sourceFileIdentity = fileIdentifier.apply(sourceFile);
			final HashEnum<File> targetFiles = indexFiles.get(sourceFileIdentity);
			final File    matchingTargetFile = searchMatchingFile(sourceFile, targetFiles, srcBaseLength, trgBaseLength);
			if(matchingTargetFile == null)
			{
				// copy source file? But then this tool becomes a complete file syncher instead of just a move-syncher.
				continue;
			}
			
			final File newTargetDirectory = new File(targetDirectoryBase, relativeSourcePath);
			final File newTargetFile      = new File(newTargetDirectory, matchingTargetFile.getName());
			
			// (01.06.2019 TM)FIXME: /!\ DEBUG
			System.out.println("$ " + sourceFile.getAbsolutePath());
			System.out.println("> " + newTargetFile.getAbsolutePath());
			System.out.println("< " + matchingTargetFile.getAbsolutePath());
			System.out.println();
			
			move(matchingTargetFile, newTargetFile);
			
			// handle other files in enum? (warn / delete, maybe functional)
		}
	}
	
	static void move(
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
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	static final void synchMoveSubDirs(
		final String                              sourceDirectoryBase,
		final File                                targetDirectoryBase,
		final int                                 srcBaseLength      ,
		final int                                 trgBaseLength      ,
		final File[]                              sourceFiles        ,
		final EqHashTable<String, HashEnum<File>> indexFiles         ,
		final Function<File, String>              fileIdentifier
	)
	{
		for(final File sourceFile : sourceFiles)
		{
			if(!sourceFile.isDirectory())
			{
				continue;
			}
			
			synchMoveDirectoryContent(
				sourceDirectoryBase,
				sourceFile,
				targetDirectoryBase,
				srcBaseLength,
				trgBaseLength,
				indexFiles,
				fileIdentifier
			);
		}
	}
	
	static File searchMatchingFile(
		final File                               sourceFile               ,
		final XGettingCollection<? extends File> targetFiles              ,
		final int                                sourceDirectoryBaseLength,
		final int                                targetDirectoryBaseLength
	)
	{
		if(targetFiles == null)
		{
			return null;
		}

		final String relativeSourcePath = getRelativePath(sourceFile.getParentFile(), sourceDirectoryBaseLength);

		// select most suited file ? (e.g. by relative path backwards)
		for(final File f : targetFiles)
		{
			final String relativeTargetPath = getRelativePath(f.getParentFile(), targetDirectoryBaseLength);
			if(relativeSourcePath.equals(relativeTargetPath))
			{
				// if there is already a matching file, discard all other potential matches
				return null;
			}
		}
		
		// naive strategy: the first file is used.
		return targetFiles.get();
	}
	
	
	
	public static void main(final String[] args)
	{
		final FileMoveSyncher fms = new FileMoveSyncher(FileMoveSyncher.fileIdentitySimpleNameSizeChangeTime());
		fms.moveSynch(
			new File("G:\\media"),
			new File("H:\\media")
		);
	}
	
}
