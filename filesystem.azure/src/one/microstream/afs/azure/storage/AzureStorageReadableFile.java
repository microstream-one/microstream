package one.microstream.afs.azure.storage;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;

public interface AzureStorageReadableFile extends AReadableFile, AzureStorageFileWrapper
{
    public static AzureStorageReadableFile New(
    	final AFile  actual,
    	final Object user  ,
    	final AzureStoragePath path
    )
    {
        return new AzureStorageReadableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }


	public class Default<U> extends AzureStorageFileWrapper.Abstract<U> implements AzureStorageReadableFile
	{
		protected Default(
			final AFile  actual,
			final U      user  ,
			final AzureStoragePath path
		)
		{
			super(actual, user, path);
		}

	}

}
