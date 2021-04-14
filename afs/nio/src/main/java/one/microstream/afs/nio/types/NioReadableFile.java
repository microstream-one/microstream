package one.microstream.afs.nio.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AReadableFile;
import one.microstream.collections.XArrays;

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
    		 * (only) readable files don't allow mutating operations.
    		 * To be overridden and replaced by writing sub class.
    		 */
    		if(XArrays.contains(options, StandardOpenOption.WRITE)
    		|| XArrays.contains(options, StandardOpenOption.APPEND)
    		|| XArrays.contains(options, StandardOpenOption.DELETE_ON_CLOSE)
    		|| XArrays.contains(options, StandardOpenOption.CREATE)
    		|| XArrays.contains(options, StandardOpenOption.CREATE_NEW)
    		)
    		{
				throw new IllegalArgumentException(
					"Invalid " + OpenOption.class.getSimpleName()
					+ "s for type " + this.getClass() + ": " + Arrays.toString(options)
				);
    		}
    	}
    	
    	@Override
    	protected OpenOption[] normalizeOpenOptions(final OpenOption... options)
    	{
    		final OpenOption[] superOptions = super.normalizeOpenOptions(options);
    		
    		return XArrays.ensureContained(superOptions, StandardOpenOption.READ);
    	}
                
    }

}
