package one.microstream.afs.blobstore.types;

import one.microstream.afs.types.AItem;

public interface BlobStoreItemWrapper extends AItem
{
	public BlobStorePath path();
}
