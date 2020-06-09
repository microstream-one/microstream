package one.microstream.afs.googlecloud.storage;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AWritableFile;

public interface GcStorageWritableFile extends GcStorageReadableFile, AWritableFile
{

    public static GcStorageWritableFile New(
    	final AFile         actual,
    	final Object        user  ,
    	final GcStoragePath path
    )
    {
        return new GcStorageWritableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }


	public class Default<U> extends GcStorageReadableFile.Default<U> implements GcStorageWritableFile
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
