package one.microstream.afs.azure.storage;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AWritableFile;

public interface AzureStorageWritableFile extends AzureStorageReadableFile, AWritableFile
{

    public static AzureStorageWritableFile New(
    	final AFile            actual,
    	final Object           user  ,
    	final AzureStoragePath path
    )
    {
        return new AzureStorageWritableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }


	public class Default<U> extends AzureStorageReadableFile.Default<U> implements AzureStorageWritableFile
    {
		protected Default(
			final AFile            actual,
			final U                user  ,
			final AzureStoragePath path
		)
		{
			super(actual, user, path);
		}

    }

}
