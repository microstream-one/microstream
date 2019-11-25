package one.microstream.persistence.test;

import one.microstream.io.XPaths;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;

public class MainTestTypeDictionaryParser
{
	public static void main(final String[] args)
	{
		final PersistenceTypeDictionary dictionary = BinaryPersistence.provideTypeDictionaryFromFile(
			XPaths.Path("c:/Files/StateDefinitions.txt")
		);
		System.out.println(dictionary);
	}
}
