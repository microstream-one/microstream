package one.microstream.persistence.test;

import java.nio.file.Path;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.functional.XFunc;
import one.microstream.io.XIO;
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
			BinaryPersistence.provideTypeDictionaryFromFile(XIO.Path("C:/Files/PersistenceTypeDictionary.ptd")),
			X.List(XIO.Path("C:/Files/export/csv/ExportTest.csv")),
			XIO.Path("C:/Files/export/bin2"),
			XFunc.all()
		);
	}

	static Path convertCsvToBin(
		final PersistenceTypeDictionary typeDictionary ,
		final XGettingCollection<Path>  binaryFiles    ,
		final Path                      targetDirectory,
		final Predicate<? super Path>   filter
	)
	{
		final StorageDataConverterTypeCsvToBinary<StorageFile> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			typeDictionary,
			new StorageEntityTypeConversionFileProvider.Default(
				targetDirectory, "dat"
			)
		);
		
		for(final Path file : binaryFiles)
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
