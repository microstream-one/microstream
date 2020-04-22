package one.microstream.afs.local;

import java.nio.file.Path;

import one.microstream.afs.AItem;


public interface LocalItem extends AItem
{
	@Override
	public LocalDirectory parent();
	
	public Path rawPath();
	
}
