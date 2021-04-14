package one.microstream.afs.nio.types;

import java.nio.file.Path;

import one.microstream.afs.types.AItem;

public interface NioItemWrapper extends AItem
{
	public Path path();
}
