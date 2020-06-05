package one.microstream.afs.nio;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.channels.FileChannel;
import java.nio.file.Path;

import one.microstream.afs.AFile;
import one.microstream.afs.AWritableFile;

public interface NioWritableFile extends NioReadableFile, AWritableFile
{
	public static NioWritableFile New(
        final AFile  actual,
        final Object user  ,
        final Path   path
    )
    {
        return NioWritableFile.New(actual, user, path, null);
    }
    
    public static NioWritableFile New(
    	final AFile       actual     ,
    	final Object      user       ,
    	final Path        path       ,
    	final FileChannel fileChannel
    )
    {
        return new NioWritableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)  ,
            mayNull(fileChannel)
        );
    }
    
    public class Default<U> extends NioReadableFile.Default<U> implements NioWritableFile
    {
        protected Default(
        	final AFile       actual     ,
        	final U           user       ,
        	final Path        path       ,
        	final FileChannel fileChannel
        )
        {
            super(actual, user, path, fileChannel);
        }
                
    }
}
