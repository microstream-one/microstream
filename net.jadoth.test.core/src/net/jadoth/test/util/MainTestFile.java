package net.jadoth.test.util;

import static net.jadoth.file.JadothFiles.buildFile;

import java.io.File;

public class MainTestFile
{
	public static void main(final String[] args)
	{
		final File f = buildFile("c:", "my", "sub", "dir", "file.txt");
		System.out.println(f);
	}
}
