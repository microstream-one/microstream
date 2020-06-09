package one.microstream.afs.aws.s3;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AWritableFile;

public interface S3WritableFile extends S3ReadableFile, AWritableFile
{

    public static S3WritableFile New(
    	final AFile  actual,
    	final Object user  ,
    	final S3Path path
    )
    {
        return new S3WritableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }


	public class Default<U> extends S3ReadableFile.Default<U> implements S3WritableFile
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
