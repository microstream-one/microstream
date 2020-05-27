package one.microstream.afs.nio;

import java.nio.channels.FileChannel;
import java.nio.file.Path;

import one.microstream.afs.temp.AFile;

public interface NioFileWrapper extends AFile.Wrapper, NioItemWrapper
{
	public FileChannel fileChannel();
	
	
	
	public abstract class Abstract<U> extends AFile.Wrapper.Abstract<U> implements NioFileWrapper
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

        private final Path        path       ;
        private       FileChannel fileChannel;
        
        
        
        ///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
        
        protected Abstract(final AFile actual, final U user, final Path path)
		{
			super(actual, user);
			this.path = path;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public Path path()
		{
			return this.path;
		}
		
		@Override
		public FileChannel fileChannel()
		{
			return this.fileChannel;
		}
        
	}
	
}
