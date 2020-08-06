package one.microstream.afs.sql;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;

public interface SqlReadableFile extends AReadableFile, SqlFileWrapper
{
    public static SqlReadableFile New(
    	final AFile   actual ,
    	final Object  user   ,
    	final SqlPath path
    )
    {
        return new SqlReadableFile.Default<>(
            notNull(actual) ,
            notNull(user)   ,
            notNull(path)
        );
    }


	public class Default<U> extends SqlFileWrapper.Abstract<U> implements SqlReadableFile
	{
		protected Default(
			final AFile   actual ,
			final U       user   ,
			final SqlPath path
		)
		{
			super(actual, user, path);
		}

	}

}
