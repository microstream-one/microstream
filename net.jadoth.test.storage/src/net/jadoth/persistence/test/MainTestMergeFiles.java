package net.jadoth.persistence.test;

import java.io.File;

import net.jadoth.storage.types.DEBUGStorage;

public class MainTestMergeFiles
{
	public static void main(final String[] args)
	{
		DEBUGStorage.mergeStorageFiles(
			new File("d:/merged.dat"),
			".dat",
			new File("D:/Bonus25/storage/channel_0"),
			new File("D:/Bonus25/storage/channel_1"),
			new File("D:/Bonus25/storage/channel_2"),
			new File("D:/Bonus25/storage/channel_3")
		);
	}
}
