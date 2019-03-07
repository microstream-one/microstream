package one.microstream.storage.io;

import one.microstream.collections.types.XGettingTable;

public interface ProtageReadableDirectory extends ProtageDirectory
{
	@Override
	public XGettingTable<String, ? extends ProtageReadableFile> files();
	
	@Override
	public ProtageReadableFile createFile(String fileName);
}
