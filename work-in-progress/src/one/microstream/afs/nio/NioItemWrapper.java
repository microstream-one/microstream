package one.microstream.afs.nio;

import java.nio.file.Path;

import one.microstream.afs.temp.AItem;

public interface NioItemWrapper extends AItem
{
	public Path path();
}
