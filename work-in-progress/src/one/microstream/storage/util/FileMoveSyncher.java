package one.microstream.storage.util;

import java.nio.file.Path;
import java.util.function.Function;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.io.XPaths;
import one.microstream.meta.XDebug;


public class FileMoveSyncher
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Function<Path, String> fileIdentifier;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public FileMoveSyncher(final Function<Path, String> fileIdentifier)
	{
		super();
		this.fileIdentifier = fileIdentifier;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public void moveSynch(final Path sourceDirectory, final Path targetDirectory)
	{
		UtilFileHandling.mustDirectory(sourceDirectory);
		UtilFileHandling.mustDirectory(targetDirectory);
		
		final EqHashTable<String, HashEnum<Path>> indexedFiles = EqHashTable.New();
		
		XDebug.println("Indexing files ...");
		UtilFileHandling.indexFiles(targetDirectory, indexedFiles, this.fileIdentifier);
		XDebug.println("Indexed unique files: " + indexedFiles.size());

		final String sourceDirectoryBase = XPaths.toAbsoluteNormalizedPath(sourceDirectory);
		
		removePerfectMatches(
			sourceDirectory,
			targetDirectory,
			sourceDirectoryBase.length(),
			XPaths.toAbsoluteNormalizedPath(targetDirectory).length(),
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
			XPaths.toAbsoluteNormalizedPath(targetDirectory).length(),
			indexedFiles,
			this.fileIdentifier
		);
	}
	
	private static String getRelativePath(final Path f, final int directoryBaseLength)
	{
		return f.toAbsolutePath().normalize().toString().substring(directoryBaseLength);
	}
	
	/**
	 * Perfect matches (alreary matching relative path) must be removed, otherwise, redundant copies
	 * of the same file would get moved around with every execution.
	 */
	static void removePerfectMatches(
		final Path                                sourceBaseDirectory,
		final Path                                targetBaseDirectory,
		final int                                 srcBaseLength      ,
		final int                                 trgBaseLength      ,
		final EqHashTable<String, HashEnum<Path>> indexFiles         ,
		final Function<Path, String>              fileIdentifier
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
		final Path                                sourceBaseDirectory,
		final Path                                targetBaseDirectory,
		final int                                 srcBaseLength      ,
		final int                                 trgBaseLength      ,
		final Path                                sourceDirectory    ,
		final EqHashTable<String, HashEnum<Path>> indexFiles         ,
		final Function<Path, String>              fileIdentifier
	)
	{
		final Path[] sourceFiles = XPaths.listChildrenUnchecked(sourceDirectory);
		
		for(final Path sourceFile : sourceFiles)
		{
			if(XPaths.isDirectoryUnchecked(sourceFile))
			{
				continue;
			}

			final String  sourceFileIdentity = fileIdentifier.apply(sourceFile);
			final HashEnum<Path> targetFiles = indexFiles.get(sourceFileIdentity);
			if(targetFiles == null)
			{
				continue;
			}
			
			final String relativeSourcePath = getRelativePath(sourceFile.getParent(), srcBaseLength);

			// select most suited file ? (e.g. by relative path backwards)
			targetFiles.removeBy(f ->
			{
				final String relativeTargetPath = getRelativePath(f.getParent(), trgBaseLength);
				if(relativeSourcePath.equals(relativeTargetPath))
				{
//					System.out.println("Removing perfect match " + f.getAbsolutePath());
					return true;
				}
				return false;
			});
		}
		
		for(final Path sourceFile : sourceFiles)
		{
			if(!XPaths.isDirectoryUnchecked(sourceFile))
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
		final Path                                sourceDirectory,
		final Path                                trgDirBase     ,
		final int                                 srcBaseLength  ,
		final int                                 trgBaseLength  ,
		final EqHashTable<String, HashEnum<Path>> indexFiles     ,
		final Function<Path, String>              fileIdentifier
	)
	{
		final Path[] sourceFiles = XPaths.listChildrenUnchecked(sourceDirectory);
		final String relativeSourcePath = getRelativePath(sourceDirectory, srcBaseLength);
		synchMoveActualFiles(srcDirBase, trgDirBase, srcBaseLength, trgBaseLength, relativeSourcePath, sourceFiles, indexFiles, fileIdentifier);
		synchMoveSubDirs(srcDirBase, trgDirBase, srcBaseLength, trgBaseLength, sourceFiles, indexFiles, fileIdentifier);
	}
	
	static final void synchMoveActualFiles(
		final String                              sourceDirectoryBase,
		final Path                                targetDirectoryBase,
		final int                                 srcBaseLength      ,
		final int                                 trgBaseLength      ,
		final String                              relativeSourcePath ,
		final Path[]                              sourceFiles        ,
		final EqHashTable<String, HashEnum<Path>> indexFiles         ,
		final Function<Path, String>              fileIdentifier
	)
	{
		for(final Path sourceFile : sourceFiles)
		{
			if(XPaths.isDirectoryUnchecked(sourceFile))
			{
				continue;
			}
			
			final String  sourceFileIdentity = fileIdentifier.apply(sourceFile);
			final HashEnum<Path> targetFiles = indexFiles.get(sourceFileIdentity);
			final Path    matchingTargetFile = searchMatchingFile(sourceFile, targetFiles, srcBaseLength, trgBaseLength);
			if(matchingTargetFile == null)
			{
				// copy source file? But then this tool becomes a complete file syncher instead of just a move-syncher.
				continue;
			}
			
			final Path newTargetDirectory = XPaths.Path(targetDirectoryBase, relativeSourcePath);
			final Path newTargetFile      = XPaths.Path(newTargetDirectory, XPaths.getFileName(matchingTargetFile));
			
			System.out.println("$ " + XPaths.toAbsoluteNormalizedPath(sourceFile));
			System.out.println("> " + XPaths.toAbsoluteNormalizedPath(newTargetFile));
			System.out.println("< " + XPaths.toAbsoluteNormalizedPath(matchingTargetFile));
			System.out.println();
			
			UtilFileHandling.move(matchingTargetFile, newTargetFile);
			
			// handle other files in enum? (warn / delete, maybe functional)
		}
	}
		
	static final void synchMoveSubDirs(
		final String                              sourceDirectoryBase,
		final Path                                targetDirectoryBase,
		final int                                 srcBaseLength      ,
		final int                                 trgBaseLength      ,
		final Path[]                              sourceFiles        ,
		final EqHashTable<String, HashEnum<Path>> indexFiles         ,
		final Function<Path, String>              fileIdentifier
	)
	{
		for(final Path sourceFile : sourceFiles)
		{
			if(!XPaths.isDirectoryUnchecked(sourceFile))
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
	
	static Path searchMatchingFile(
		final Path                               sourceFile               ,
		final XGettingCollection<? extends Path> targetFiles              ,
		final int                                sourceDirectoryBaseLength,
		final int                                targetDirectoryBaseLength
	)
	{
		if(targetFiles == null)
		{
			return null;
		}

		final String relativeSourcePath = getRelativePath(sourceFile.getParent(), sourceDirectoryBaseLength);

		/* (08.06.2019 TM)FIXME: select most suited file (e.g. by relative path backwards)
		 * because otherwise for duplicate files, one gets moved to another location
		 * and then it is missing (file not found exception) for the next location.
		 */
		for(final Path f : targetFiles)
		{
			final String relativeTargetPath = getRelativePath(f.getParent(), targetDirectoryBaseLength);
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
			XPaths.Path("G:\\media"),
			XPaths.Path("T:\\media")
		);
	}
	
}
