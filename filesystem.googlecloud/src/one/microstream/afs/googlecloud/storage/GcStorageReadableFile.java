package one.microstream.afs.googlecloud.storage;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;

public interface GcStorageReadableFile extends AReadableFile, GcStorageFileWrapper
{
    public static GcStorageReadableFile New(
    	final AFile         actual,
    	final Object        user  ,
    	final GcStoragePath path
    )
    {
        return new GcStorageReadableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }


	public class Default<U> extends GcStorageFileWrapper.Abstract<U> implements GcStorageReadableFile
	{
		protected Default(
			final AFile         actual,
			final U             user  ,
			final GcStoragePath path
		)
		{
			super(actual, user, path);
		}

	}

}
