package net.jadoth.storage.types;

import net.jadoth.functional._longProcedure;

public interface StorageReferenceMarker extends _longProcedure
{
	public boolean tryFlush();
}
