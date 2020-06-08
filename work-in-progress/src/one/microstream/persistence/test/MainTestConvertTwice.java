package one.microstream.persistence.test;

import java.nio.file.Path;

import one.microstream.X;
import one.microstream.functional.XFunc;
import one.microstream.io.XIO;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.ZStorageLockedFile;

@SuppressWarnings("unused")
public class MainTestConvertTwice extends TestStorage
{
	static final Path   dir      = XIO.Path("D:/zStorageConversionBug");
	static final String filename = "java.lang.String";

	public static void main(final String[] args)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(XIO.Path(dir, "csv"), "csv"),
			STORAGE.typeDictionary(),
			null,
			4096,
			4096
		);
		final ZStorageLockedFile file = ZStorageLockedFile.openLockedFile(
			XIO.Path(XIO.Path(dir, "bin"), filename + ".dat")
		);
		converter.convertDataFile(file);


		final Path bin2Dir = MainTestConvertCsvToBin.convertCsvToBin(
			STORAGE.typeDictionary(),
			X.List(XIO.Path(XIO.Path(dir, "csv"), filename + ".csv")),
			XIO.Path(dir, "bin2"),
			XFunc.all()
		);
	}
}
