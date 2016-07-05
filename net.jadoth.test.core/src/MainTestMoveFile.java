import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class MainTestMoveFile
{
	public static void main(final String[] args) throws IOException
	{
		final File source = new File("D:/testsource.bin");
		final File target = new File("D:/target/");

		Files.move(source.toPath(), target.toPath().resolve(source.getName()));
	}
}
