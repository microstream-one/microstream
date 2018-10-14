package net.jadoth.storage.io;

import net.jadoth.collections.types.XGettingTable;

public interface ProtageDirectory
{
	public String name();
	
	public XGettingTable<String, ? extends ProtageFile> files();
	
	public ProtageFile createFile(String fileName);
}
