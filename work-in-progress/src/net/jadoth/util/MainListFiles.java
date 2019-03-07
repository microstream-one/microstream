package net.jadoth.util;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MainListFiles
{
	public static void main(final String[] args) throws Exception
	{
		searchStringsInFiles(
			new File("D:/workspaces/stuff").listFiles(),
			file -> file.getName().matches("Stuff.*\\.java"),
			file -> System.out.println(file.getName())
		);
	}
	


	static void searchStringsInFiles(
		final File[]                  files ,
		final Predicate<? super File> filter,
		final Consumer<? super File>  logic
	)
	{
		for(final File f : files)
		{
			if(filter.test(f))
			{
				logic.accept(f);
			}
			if(f.isDirectory())
			{
				searchStringsInFiles(f.listFiles(), filter, logic);
			}
		}
	}

}
