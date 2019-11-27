package one.microstream.storage.util;

import java.nio.file.Path;
import java.util.function.Function;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.io.XPaths;
import one.microstream.meta.XDebug;
import one.microstream.typing.KeyValue;


public class FileDuplicateChecker
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Function<Path, String> fileIdentifier;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public FileDuplicateChecker(final Function<Path, String> fileIdentifier)
	{
		super();
		this.fileIdentifier = fileIdentifier;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public final void checkForDuplicates(final Path directory)
	{
		UtilFileHandling.mustDirectory(directory);
		
		final EqHashTable<String, HashEnum<Path>> indexedFiles = EqHashTable.New();
		
		XDebug.println("Indexing files ...");
		UtilFileHandling.indexFiles(directory, indexedFiles, this.fileIdentifier);
		XDebug.println("Indexed unique files: " + indexedFiles.size());
		
		// (02.06.2019 TM)FIXME: collect and sort by file size
		for(final KeyValue<String, HashEnum<Path>> e : indexedFiles)
		{
			if(e.value().size() > 1)
			{
				System.out.println("Duplicates:");
				e.value().iterate(System.out::println);
				System.out.println();
			}
		}
	}
	
	
	public static void main(final String[] args)
	{
		final FileDuplicateChecker fms = new FileDuplicateChecker(
			UtilFileHandling.fileIdentitySimpleNameSizeChangeTime()
		);
		fms.checkForDuplicates(
			XPaths.Path("G:\\media")
		);
	}
	
}
