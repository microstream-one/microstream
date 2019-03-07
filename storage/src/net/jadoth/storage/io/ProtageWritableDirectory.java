package net.jadoth.storage.io;

import net.jadoth.collections.types.XGettingTable;

public interface ProtageWritableDirectory extends ProtageReadableDirectory
{
	@Override
	public XGettingTable<String, ? extends ProtageWritableFile> files();
		
	@Override
	public ProtageWritableFile createFile(String fileName);
}
