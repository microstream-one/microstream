package net.jadoth.persistence.test;

import java.io.File;
import java.util.function.Predicate;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional.JadothPredicates;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.storage.types.StorageDataConverterCsvConfiguration;
import net.jadoth.storage.types.StorageDataConverterTypeCsvToBinary;
import net.jadoth.storage.types.StorageEntityTypeConversionFileProvider;

public class MainTestConvertCsvToBin
{
	public static void main(final String[] args)
	{
		convertCsvToBin(
			BinaryPersistence.provideTypeDictionaryFromFile(new File("C:/Files/PersistenceTypeDictionary.ptd")),
			X.List(new File("C:/Files/export/csv/de.emverbund.bonus.stammdaten.Datenstand$Implementation.csv")),
			new File("C:/Files/export/bin2"),
			JadothPredicates.all()
		);
	}

	static File convertCsvToBin(
		final PersistenceTypeDictionary typeDictionary ,
		final XGettingCollection<File>  binaryFiles    ,
		final File                      targetDirectory,
		final Predicate<? super File>   filter
	)
	{
		final StorageDataConverterTypeCsvToBinary<File> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			typeDictionary,
			new StorageEntityTypeConversionFileProvider.Implementation(
				targetDirectory, "dat"
			)
		);
		
		for(final File file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			try
			{
				converter.convertCsv(file);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file "+file, e);
			}
		}

		
		return targetDirectory;
	}

}
