package one.microstream.afs.blobstore.types;

import static one.microstream.X.notNull;

import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AReadableFile;

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
