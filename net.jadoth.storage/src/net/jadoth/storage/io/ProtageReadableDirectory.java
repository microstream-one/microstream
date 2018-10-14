package net.jadoth.storage.io;

import net.jadoth.collections.types.XGettingTable;

public interface ProtageReadableDirectory extends ProtageDirectory
{
	@Override
	public XGettingTable<String, ? extends ProtageReadableFile> files();
	
	@Override
	public ProtageReadableFile createFile(String fileName);
}
