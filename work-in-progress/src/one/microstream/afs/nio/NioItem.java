package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.AItem;


public interface NioItem extends AItem
{
	@Override
	public NioDirectory parent();
	
	public Path rawPath();
	
}
