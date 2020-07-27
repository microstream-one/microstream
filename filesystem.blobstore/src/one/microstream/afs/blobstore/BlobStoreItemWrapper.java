package one.microstream.afs.blobstore;

import one.microstream.afs.AItem;

public interface BlobStoreItemWrapper extends AItem
{
	public BlobStorePath path();
}
