package one.microstream.afs.nio.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AWritableFile;
import one.microstream.collections.XArrays;

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
    	///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
    	
        protected Default(
        	final AFile       actual     ,
        	final U           user       ,
        	final Path        path       ,
        	final FileChannel fileChannel
        )
        {
            super(actual, user, path, fileChannel);
        }
        
        
        
        ///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
        
        @Override
        protected void validateOpenOptions(final OpenOption... options)
        {
        	/*
        	 * override super class (readable) implementation to do no validation
        	 * since everything is allowed for writable files.
        	 */
        }
        
        @Override
        protected OpenOption[] normalizeOpenOptions(final OpenOption... options)
        {
        	// super class implementation ensures READ
    		final OpenOption[] superOptions = super.normalizeOpenOptions(options);
    		
    		// this implementation ensures WRITE
    		return XArrays.ensureContained(superOptions, StandardOpenOption.WRITE);
        }
                
    }
    
}
