package one.microstream.afs.blobstore;

public interface BlobStorePathVisitor
{
	public void visitDirectory(BlobStorePath parent, String directoryName);

	public void visitFile(BlobStorePath parent, String fileName);
}
