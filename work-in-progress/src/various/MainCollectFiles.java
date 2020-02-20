package various;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import one.microstream.io.XIO;


public class MainCollectFiles
{
	static final Path sourceDirectory = XIO.Path("C:\\");

	static final Path targetDirectory = XIO.Path("D:\\lolz");

	public static void main(final String[] args) throws Throwable
	{
		collect(sourceDirectory, "txt");
		collect(sourceDirectory, "rtf");
		collect(sourceDirectory, "jpg");
		collect(sourceDirectory, "png");
		collect(sourceDirectory, "gif");
		collect(sourceDirectory, "doc");
		collect(sourceDirectory, "xls");
		collect(sourceDirectory, "pdf");
		collect(sourceDirectory, "exe");
		collect(sourceDirectory, "dll");
		collect(sourceDirectory, "ini");
		collect(sourceDirectory, "wav");
		collect(sourceDirectory, "avi");
		collect(sourceDirectory, "mp3");
	}


	static final void collect(final Path sourceDirectory, final String fileType) throws IOException
	{
		final String fileSuffix = XIO.fileSuffixSeparator() + fileType.toLowerCase();
		final Path   targetDir  = XIO.Path(targetDirectory, fileType.toUpperCase());

		processFiles(
			sourceDirectory,
			fileSuffix,
			filePath -> {
				System.out.println(filePath);
				XIO.unchecked.ensureDirectoryAndFile(XIO.Path(targetDir, filePath));
			}
		);
	}


	static final void processFiles(
		final Path                     sourceDirectory,
		final String                   type           ,
		final Consumer<? super String> logic
	)
		throws IOException
	{
		final Path[] files = XIO.unchecked.listEntries(sourceDirectory);
		if(files == null)
		{
			return;
		}

		for(final Path file : files)
		{
			if(XIO.unchecked.isDirectory(file))
			{
				processFiles(file, type, logic);
				continue;
			}
			final String path = XIO.toAbsoluteNormalizedPath(file);
			if(!path.substring(path.length() - type.length(), path.length()).toLowerCase().equals(type))
			{
				continue;
			}

			final String name    = XIO.getFileName(file);
			final String dirPart = path.substring(0, path.length() - name.length() - 1);
			final String result  = dirPart.substring(3, dirPart.length()).replace('\\', '_')+'!'+name;
			logic.accept(result);
		}

	}

}
