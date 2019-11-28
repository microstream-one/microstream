package one.microstream.persistence.test;

import java.nio.file.Path;

import one.microstream.X;
import one.microstream.collections.types.XList;
import one.microstream.io.XIO;

public class MainTestMergeFiles
{
	public static void main(final String[] args)
	{
		mergeStorageFiles(
			XIO.Path("d:/merged.dat"),
			".dat",
			XIO.Path("D:/Bonus25/storage/channel_0"),
			XIO.Path("D:/Bonus25/storage/channel_1"),
			XIO.Path("D:/Bonus25/storage/channel_2"),
			XIO.Path("D:/Bonus25/storage/channel_3")
		);
	}

	public static final void mergeStorageFiles(
		final Path    targetFile        ,
		final String  fileSuffix        ,
		final Path... channelDirectories
	)
	{
		XIO.ensureWriteableFileUnchecked(targetFile);

		final XList<Path> sourceFiles = X.List();
		for(final Path file : channelDirectories)
		{
			XIO.listEntriesUnchecked(file, sourceFiles);
		}

		XIO.mergeBinary(sourceFiles, targetFile, f -> f.toString().endsWith(fileSuffix));
	}
}
