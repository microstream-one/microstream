package one.microstream.afs.nio;

import static one.microstream.X.notNull;

import java.nio.file.Path;

import one.microstream.afs.temp.AFile;
import one.microstream.afs.temp.AWritableFile;

public interface NioWritableFile extends NioFileWrapper, AWritableFile
{
    public static NioWritableFile New(
        final AFile  actual,
        final Object user  ,
        final Path   path
    )
    {
        return new NioWritableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)
        );
    }
    
    public class Default<U> extends NioFileWrapper.Abstract<U> implements NioWritableFile
    {
        protected Default(final AFile actual, final U user, final Path path)
        {
            super(actual, user, path);
        }
                
    }
}
