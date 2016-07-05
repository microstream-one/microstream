package net.jadoth.persistence.test;

import java.io.File;

import net.jadoth.persistence.binary.types.BinaryFieldLengthResolver;
import net.jadoth.persistence.internal.FilePersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeDictionaryParser;

public class MainTestTypeDictionaryParser
{
	public static void main(final String[] args)
	{
		final String input = FilePersistenceTypeDictionary.readTypeDictionary(new File("c:/Files/StateDefinitions.txt"));

		final PersistenceTypeDictionary dictionary = new PersistenceTypeDictionaryParser.Implementation(
			new BinaryFieldLengthResolver.Implementation()
		).parse(input);
		System.out.println(dictionary);
	}
}
