package one.microstream.afs;

import one.microstream.collections.types.XGettingTable;

public interface ProtageReadableDirectory extends ADirectory
{
	@Override
	public XGettingTable<String, ? extends ProtageReadableFile> files();
	
	@Override
	public ProtageReadableFile createFile(String fileName);
}
