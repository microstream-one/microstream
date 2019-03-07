package one.microstream.storage.types;

import java.io.File;

import one.microstream.X;
import one.microstream.collections.types.XList;
import one.microstream.files.XFiles;
import one.microstream.memory.XMemory;
import one.microstream.meta.XDebug;


// (22.06.2013) DEBUG class should be removed at some point.
public final class DEBUGStorage
{
	private static final boolean ENABLED = true;

	private static void internalPrintln(final String s)
	{
		if(ENABLED)
		{
			XDebug.println(s, 2);
		}

	}

	public static final void println(final String s)
	{
		internalPrintln(s);

	}


	public static final byte[] extractMemory(final long address, final int length)
	{
		final byte[] array = new byte[length];
		XMemory.copyRangeToArray(address, array);
		return array;
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



	private DEBUGStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
