package net.jadoth.persistence.test;

import java.io.File;
import java.util.function.Predicate;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.functional.JadothFunctional;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.storage.types.StorageDataConverterCsvConfiguration;
import net.jadoth.storage.types.StorageDataConverterTypeBinaryToCsv;
import net.jadoth.storage.types.StorageEntityTypeConversionFileProvider;

public class MainTestConvertBinToCsv
{
	public static void main(final String[] args)
	{
		convertBinToCsv(
			X.List(new File("C:/Files/export/bin/de.emverbund.bonus.stammdaten.Datenstand$Implementation.dat")),
			JadothFunctional.all()
		);
	}

	static void convertBinToCsv(final XGettingCollection<File> binaryFiles, final Predicate<? super File> filter)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.ImplementationUTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Implementation(
				new File(binaryFiles.get().getParentFile().getParentFile(), "csv"), "csv"
			),
			BinaryPersistence.provideTypeDictionaryFromFile(new File("C:/Files/PersistenceTypeDictionary.ptd")),
			null,
			1<<20,
			1<<20
		);

		for(final File file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			converter.convertDataFile(file);
		}
	}

}
