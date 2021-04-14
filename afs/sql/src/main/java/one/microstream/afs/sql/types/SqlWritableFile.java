package one.microstream.afs.sql.types;

import static one.microstream.X.notNull;

import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AWritableFile;

public interface SqlWritableFile extends SqlReadableFile, AWritableFile
{

    public static SqlWritableFile New(
    	final AFile   actual ,
    	final Object  user   ,
    	final SqlPath path
    )
    {
        return new SqlWritableFile.Default<>(
            notNull(actual) ,
            notNull(user)   ,
            notNull(path)
        );
    }


	public class Default<U> extends SqlReadableFile.Default<U> implements SqlWritableFile
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
