package one.microstream.persistence.test;

import java.io.File;

import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;

public class MainTestTypeDictionaryParser
{
	public static void main(final String[] args)
	{
		final PersistenceTypeDictionary dictionary = BinaryPersistence.provideTypeDictionaryFromFile(
			new File("c:/Files/StateDefinitions.txt")
		);
		System.out.println(dictionary);
	}
}
