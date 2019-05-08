package one.microstream.persistence.test;

import java.io.File;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.functional.XFunc;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeCsvToBinary;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageFile;
import one.microstream.storage.types.StorageLockedFile;

public class MainTestConvertCsvToBin
{
	public static void main(final String[] args)
	{
		convertCsvToBin(
			BinaryPersistence.provideTypeDictionaryFromFile(new File("C:/Files/PersistenceTypeDictionary.ptd")),
			X.List(new File("C:/Files/export/csv/ExportTest.csv")),
			new File("C:/Files/export/bin2"),
			XFunc.all()
		);
	}

	static File convertCsvToBin(
		final PersistenceTypeDictionary typeDictionary ,
		final XGettingCollection<File>  binaryFiles    ,
		final File                      targetDirectory,
		final Predicate<? super File>   filter
	)
	{
		final StorageDataConverterTypeCsvToBinary<StorageFile> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			typeDictionary,
			new StorageEntityTypeConversionFileProvider.Default(
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
				final StorageLockedFile storageFile = StorageLockedFile.openLockedFile(file);
				converter.convertCsv(storageFile);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file "+file, e);
			}
		}

		
		return targetDirectory;
	}

}
