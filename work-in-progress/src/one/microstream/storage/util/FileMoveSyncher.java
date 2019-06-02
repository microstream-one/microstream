package one.microstream.storage.util;

import java.io.File;
import java.util.function.Function;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.meta.XDebug;


public class FileMoveSyncher
{
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
		UtilFileHandling.mustDirectory(sourceDirectory);
		UtilFileHandling.mustDirectory(targetDirectory);
		
		final EqHashTable<String, HashEnum<File>> indexedFiles = EqHashTable.New();
		
		XDebug.println("Indexing files ...");
		UtilFileHandling.indexFiles(targetDirectory, indexedFiles, this.fileIdentifier);
		XDebug.println("Indexed unique files: " + indexedFiles.size());

		final String sourceDirectoryBase = sourceDirectory.getAbsolutePath();
		
		removePerfectMatches(
			sourceDirectory,
			targetDirectory,
			sourceDirectoryBase.length(),
			targetDirectory.getAbsolutePath().length(),
			indexedFiles,
			this.fileIdentifier
		);
		
//		checkForDuplicates(indexedFiles);
		
		XDebug.println("Synching files ...");
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
	
	private static String getRelativePath(final File f, final int directoryBaseLength)
	{
		return f.getAbsolutePath().substring(directoryBaseLength);
	}
	
	/**
	 * Perfect matches (alreary matching relative path) must be removed, otherwise, redundant copies
	 * of the same file would get moved around with every execution.
	 */
	static void removePerfectMatches(
		final File                                sourceBaseDirectory,
		final File                                targetBaseDirectory,
		final int                                 srcBaseLength      ,
		final int                                 trgBaseLength      ,
		final EqHashTable<String, HashEnum<File>> indexFiles         ,
		final Function<File, String>              fileIdentifier
	)
	{
		removePerfectMatches(
			sourceBaseDirectory,
			targetBaseDirectory,
			srcBaseLength      ,
			trgBaseLength      ,
			sourceBaseDirectory,
			indexFiles,
			fileIdentifier
		);
	}
	
	static void removePerfectMatches(
		final File                                sourceBaseDirectory,
		final File                                targetBaseDirectory,
		final int                                 srcBaseLength      ,
		final int                                 trgBaseLength      ,
		final File                                sourceDirectory    ,
		final EqHashTable<String, HashEnum<File>> indexFiles         ,
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

			final String  sourceFileIdentity = fileIdentifier.apply(sourceFile);
			final HashEnum<File> targetFiles = indexFiles.get(sourceFileIdentity);
			if(targetFiles == null)
			{
				continue;
			}
			
			final String relativeSourcePath = getRelativePath(sourceFile.getParentFile(), srcBaseLength);

			// select most suited file ? (e.g. by relative path backwards)
			targetFiles.removeBy(f ->
			{
				final String relativeTargetPath = getRelativePath(f.getParentFile(), trgBaseLength);
				if(relativeSourcePath.equals(relativeTargetPath))
				{
					System.out.println("Removing perfect match " + f.getAbsolutePath());
					return true;
				}
				return false;
			});
		}
		
		for(final File sourceFile : sourceFiles)
		{
			if(!sourceFile.isDirectory())
			{
				continue;
			}
			removePerfectMatches(
				sourceBaseDirectory,
				targetBaseDirectory,
				srcBaseLength,
				trgBaseLength,
				sourceFile,
				indexFiles,
				fileIdentifier
			);
		}
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
			
			UtilFileHandling.move(matchingTargetFile, newTargetFile);
			
			// handle other files in enum? (warn / delete, maybe functional)
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
		final FileMoveSyncher fms = new FileMoveSyncher(UtilFileHandling.fileIdentitySimpleNameSizeChangeTime());
		fms.moveSynch(
			new File("G:\\media"),
			new File("H:\\media")
		);
	}
	
}
