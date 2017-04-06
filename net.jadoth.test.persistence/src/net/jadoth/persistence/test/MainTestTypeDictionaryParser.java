package net.jadoth.persistence.test;

import java.io.File;

import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDictionary;

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
