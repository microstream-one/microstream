package net.jadoth.util;

import static net.jadoth.meta.JadothConsole.debugln;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.jadoth.util.file.JadothFiles;

public class UtilResetDirectory
{
	public static void resetDirecory(final File target, final File source, final boolean output) throws IOException
	{
		deleteAllFiles(target, output);
		copyFile(source, source, target);
	}

	public static final void deleteAllFiles(final File directory, final boolean output)
	{
		if(!directory.exists())
		{
			return;
		}
		for(final File f : directory.listFiles())
		{
			if(f.isDirectory())
			{
				deleteAllFiles(f, output);
			}
			try
			{
				if(output)
				{
					debugln("Deleting "+f);
				}
				Files.deleteIfExists(f.toPath());
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Cannot delete file: "+f, e);
			}
		}

	}

	public static void copyFile(final File sourceRoot, final File subject, final File targetRoot) throws IOException
	{
		if(subject.isDirectory())
		{
			copyDirectory(sourceRoot, subject, targetRoot);
		}
		else
		{
			copyActualFile(sourceRoot, subject, targetRoot);
		}
	}

	public static void copyDirectory(final File sourceRoot, final File subject, final File targetRoot) throws IOException
	{
		for(final File file : subject.listFiles())
		{
			copyFile(sourceRoot, file, targetRoot);
		}
	}

	public static void copyActualFile(final File sourceRoot, final File subject, final File targetRoot) throws IOException
	{
		final String sourceRootPath = sourceRoot.getAbsolutePath();
		final String subjectPath    = subject.getAbsolutePath();
		final File   targetFile     = new File(targetRoot, subjectPath.substring(sourceRootPath.length()));

		JadothFiles.ensureDirectoryAndFile(targetFile);

		final Path sourcePath      = subject.toPath();
		final Path destinationPath = targetFile.toPath();

		Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
	}

}
