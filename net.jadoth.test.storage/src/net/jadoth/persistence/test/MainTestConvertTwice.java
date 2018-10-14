package net.jadoth.persistence.test;

import java.io.File;

import net.jadoth.X;
import net.jadoth.functional.XFunc;
import net.jadoth.storage.types.StorageDataConverterCsvConfiguration;
import net.jadoth.storage.types.StorageDataConverterTypeBinaryToCsv;
import net.jadoth.storage.types.StorageEntityTypeConversionFileProvider;
import net.jadoth.storage.types.StorageLockedFile;

@SuppressWarnings("unused")
public class MainTestConvertTwice extends TestStorage
{
	static final File   dir      = new File("D:/zStorageConversionBug");
	static final String filename = "java.lang.String";

	public static void main(final String[] args)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.ImplementationUTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Implementation(new File(dir, "csv"), "csv"),
			STORAGE.typeDictionary(),
			null,
			4096,
			4096
		);
		final StorageLockedFile file = StorageLockedFile.openLockedFile(
			new File(new File(dir, "bin"), filename + ".dat")
		);
		converter.convertDataFile(file);


		final File bin2Dir = MainTestConvertCsvToBin.convertCsvToBin(
			STORAGE.typeDictionary(),
			X.List(new File(new File(dir, "csv"), filename + ".csv")),
			new File(dir, "bin2"),
			XFunc.all()
		);
	}
}
