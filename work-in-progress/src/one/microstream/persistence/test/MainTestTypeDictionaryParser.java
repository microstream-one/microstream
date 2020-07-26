package one.microstream.persistence.test;

import one.microstream.afs.nio.NioFileSystem;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;

public class MainTestTypeDictionaryParser
{
	public static void main(final String[] args)
	{
		final PersistenceTypeDictionary dictionary = BinaryPersistence.provideTypeDictionaryFromFile(
			NioFileSystem.file("c:/Files/StateDefinitions.txt")
		);
		System.out.println(dictionary);
	}
}
