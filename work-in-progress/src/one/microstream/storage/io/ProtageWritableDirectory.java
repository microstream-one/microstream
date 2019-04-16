package one.microstream.storage.io;

import one.microstream.collections.types.XGettingTable;

public interface ProtageWritableDirectory extends ProtageReadableDirectory
{
	@Override
	public XGettingTable<String, ? extends ProtageWritableFile> files();
		
	@Override
	public ProtageWritableFile createFile(String fileName);
}
