import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import net.jadoth.util.file.JadothFiles;


public class MainCollectFiles
{
	static final File sourceDirectory = new File("C:\\");

	static final File targetDirectory = new File("D:\\lolz");

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


	static final void collect(final File sourceDirectory, final String fileType) throws IOException
	{
		final String fileSuffix = '.'+fileType.toLowerCase();
		final File targetDir = new File(targetDirectory, fileType.toUpperCase());

		processFiles(
			sourceDirectory,
			fileSuffix,
			filePath -> {
				System.out.println(filePath);
				JadothFiles.ensureDirectoryAndFile(new File(targetDir, filePath));
			}
		);
	}


	static final void processFiles(
		final File                      sourceDirectory,
		final String                    type           ,
		final Consumer<? super String> logic
	)
		throws IOException
	{
		final File[] files = sourceDirectory.listFiles();
		if(files == null)
		{
			return;
		}

		for(final File file : files)
		{
			if(file.isDirectory())
			{
				processFiles(file, type, logic);
				continue;
			}
			final String path = file.getAbsolutePath();
			if(!path.substring(path.length() - type.length(), path.length()).toLowerCase().equals(type))
			{
				continue;
			}

			final String name    = file.getName();
			final String dirPart = path.substring(0, path.length() - name.length() - 1);
			final String result  = dirPart.substring(3, dirPart.length()).replace('\\', '_')+'!'+name;
			logic.accept(result);
		}

	}

}
