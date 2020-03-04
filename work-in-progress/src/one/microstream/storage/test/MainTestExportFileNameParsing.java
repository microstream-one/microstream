package one.microstream.storage.test;

import one.microstream.storage.types.StorageEntityTypeExportFileProvider;

public class MainTestExportFileNameParsing
{
	
	public static void main(final String[] args)
	{
		final String fileName = StorageEntityTypeExportFileProvider.toUniqueTypeFileName("MyType", 1234);
		System.out.println("Assembled type file name: >" + fileName + "<");
		final long typeId = StorageEntityTypeExportFileProvider.getTypeIdFromUniqueTypeFileName(fileName);
		System.out.println("TypeId parsed from assembled file name: >" + typeId + "<");
	}
	
}
