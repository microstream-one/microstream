package one.microstream.afs.blobstore;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;

public interface BlobStoreReadableFile extends AReadableFile, BlobStoreFileWrapper
{
    public static BlobStoreReadableFile New(
    	final AFile         actual,
    	final Object        user  ,
    	final BlobStorePath path
    )
    {
        return new BlobStoreReadableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }


	public class Default<U> extends BlobStoreFileWrapper.Abstract<U> implements BlobStoreReadableFile
	{
		protected Default(
			final AFile         actual,
			final U             user  ,
			final BlobStorePath path
		)
		{
			super(actual, user, path);
		}

	}

}
