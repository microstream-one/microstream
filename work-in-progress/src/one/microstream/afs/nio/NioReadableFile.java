package one.microstream.afs.nio;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.channels.FileChannel;
import java.nio.file.Path;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;

public interface NioReadableFile extends AReadableFile, NioFileWrapper
{
    public static NioReadableFile New(
        final AFile  actual,
        final Object user  ,
        final Path   path
    )
    {
        return NioReadableFile.New(actual, user, path, null);
    }
        
    public static NioReadableFile New(
    	final AFile       actual     ,
    	final Object      user       ,
    	final Path        path       ,
    	final FileChannel fileChannel
    )
    {
        return new NioReadableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)  ,
            mayNull(fileChannel)
        );
    }
    
    public class Default<U> extends NioFileWrapper.Abstract<U> implements NioReadableFile
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
