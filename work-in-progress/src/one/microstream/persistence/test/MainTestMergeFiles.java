package one.microstream.persistence.test;

import java.io.File;

import one.microstream.storage.types.DEBUGStorage;

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
