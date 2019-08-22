package one.microstream.persistence.test;

import java.io.File;

import one.microstream.X;
import one.microstream.collections.types.XList;
import one.microstream.files.XFiles;

public class MainTestMergeFiles
{
	public static void main(final String[] args)
	{
		mergeStorageFiles(
			new File("d:/merged.dat"),
			".dat",
			new File("D:/Bonus25/storage/channel_0"),
			new File("D:/Bonus25/storage/channel_1"),
			new File("D:/Bonus25/storage/channel_2"),
			new File("D:/Bonus25/storage/channel_3")
		);
	}

	public static final void mergeStorageFiles(
		final File    targetFile        ,
		final String  fileSuffix        ,
		final File... channelDirectories
	)
	{
		XFiles.ensureWriteableFile(targetFile);

		final XList<File> sourceFiles = X.List();
		for(final File file : channelDirectories)
		{
			sourceFiles.addAll(file.listFiles());
		}

		XFiles.mergeBinary(sourceFiles, targetFile, f -> f.getPath().endsWith(fileSuffix));
	}
}
