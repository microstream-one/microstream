package one.microstream.afs.blobstore.types;

public interface BlobStorePathVisitor
{
	public void visitDirectory(BlobStorePath parent, String directoryName);

	public void visitFile(BlobStorePath parent, String fileName);
}
