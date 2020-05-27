package one.microstream.afs.nio;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.afs.temp.AFile;
import one.microstream.afs.temp.AReadableFile;

public interface NioReadableFile extends AReadableFile, NioFileWrapper
{
    public static NioReadableFile New(
        final AFile  actual,
        final Object user  ,
        final Path   path
    )
    {
        return new NioReadableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }
    
    public class Default<U> extends NioFileWrapper.Abstract<U> implements NioReadableFile
    {
    	protected Default(final AFile actual, final U user, final Path path)
        {
            super(actual, user, path);
        }
                
    }

}
