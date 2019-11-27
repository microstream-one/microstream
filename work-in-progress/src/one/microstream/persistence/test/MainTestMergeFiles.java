package one.microstream.persistence.test;

import java.nio.file.Path;

import one.microstream.X;
import one.microstream.collections.types.XList;
import one.microstream.io.XPaths;

public class MainTestMergeFiles
{
	public static void main(final String[] args)
	{
		mergeStorageFiles(
			XPaths.Path("d:/merged.dat"),
			".dat",
			XPaths.Path("D:/Bonus25/storage/channel_0"),
			XPaths.Path("D:/Bonus25/storage/channel_1"),
			XPaths.Path("D:/Bonus25/storage/channel_2"),
			XPaths.Path("D:/Bonus25/storage/channel_3")
		);
	}

	public static final void mergeStorageFiles(
		final Path    targetFile        ,
		final String  fileSuffix        ,
		final Path... channelDirectories
	)
	{
		XPaths.ensureWriteableFileUnchecked(targetFile);

		final XList<Path> sourceFiles = X.List();
		for(final Path file : channelDirectories)
		{
			XPaths.listChildrenUnchecked(file, sourceFiles);
		}

		XPaths.mergeBinary(sourceFiles, targetFile, f -> f.toString().endsWith(fileSuffix));
	}
}
