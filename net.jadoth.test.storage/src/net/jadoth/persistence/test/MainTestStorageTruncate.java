package net.jadoth.persistence.test;



public class MainTestStorageTruncate extends TestStorage
{

	public static void main(final String[] args)
	{
		System.gc();
		STORAGE.truncateData();
		System.gc();
		exit();
	}

}
