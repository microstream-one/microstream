package one.microstream.test.util;

import static one.microstream.files.XFiles.buildFile;

import java.io.File;

public class MainTestFile
{
	public static void main(final String[] args)
	{
		final File f = buildFile("c:", "my", "sub", "dir", "file.txt");
		System.out.println(f);
	}
}
