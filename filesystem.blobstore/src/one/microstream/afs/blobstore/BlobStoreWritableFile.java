package one.microstream.afs.blobstore;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AWritableFile;

public interface BlobStoreWritableFile extends BlobStoreReadableFile, AWritableFile
{

    public static BlobStoreWritableFile New(
    	final AFile         actual,
    	final Object        user  ,
    	final BlobStorePath path
    )
    {
        return new BlobStoreWritableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }


	public class Default<U> extends BlobStoreReadableFile.Default<U> implements BlobStoreWritableFile
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
