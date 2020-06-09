package one.microstream.afs.aws.s3;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;

public interface S3ReadableFile extends AReadableFile, S3FileWrapper
{
    public static S3ReadableFile New(
    	final AFile  actual,
    	final Object user  ,
    	final S3Path path
    )
    {
        return new S3ReadableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }


	public class Default<U> extends S3FileWrapper.Abstract<U> implements S3ReadableFile
	{
		protected Default(
			final AFile  actual,
			final U      user  ,
			final S3Path path
		)
		{
			super(actual, user, path);
		}

	}

}
