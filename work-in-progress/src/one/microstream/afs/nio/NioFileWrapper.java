package one.microstream.afs.nio;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import one.microstream.afs.AFile;
import one.microstream.chars.XChars;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.XIO;

public interface NioFileWrapper extends AFile.Wrapper, NioItemWrapper
{
	public FileChannel fileChannel();
	
	public boolean retire();
	
	public boolean isRetired();
	
	public boolean isChannelOpen();
	
	public boolean checkChannelOpen();
	
	public FileChannel ensureOpenChannel();
	
	public boolean openChannel() throws IORuntimeException;
	
	public boolean openChannel(OpenOption... options) throws IORuntimeException;
	
	public boolean reopenChannel(OpenOption... options) throws IORuntimeException;
	
	public boolean closeChannel() throws IORuntimeException;
	
	
	
	
	public abstract class Abstract<U> extends AFile.Wrapper.Abstract<U> implements NioFileWrapper
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

        Path        path       ;
        FileChannel fileChannel;
        
        
        
        ///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
        
        protected Abstract(
        	final AFile       actual     ,
        	final U           user       ,
        	final Path        path       ,
        	final FileChannel fileChannel
        )
		{
			super(actual, user);
			this.path        = notNull(path)       ;
			this.fileChannel = mayNull(fileChannel);
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
		
		@Override
		public synchronized boolean retire()
		{
			if(this.path == null)
			{
				return false;
			}
			
			this.path = null;
			
			return true;
		}
		
		@Override
		public synchronized boolean isRetired()
		{
			return this.path == null;
		}
		
		public void validateIsNotRetired()
		{
			if(!this.isRetired())
			{
				return;
			}
			
			// (28.05.2020 TM)EXCP: proper exception
			throw new RuntimeException(
				"File is retired: " + XChars.systemString(this) + "(\"" + this.toPathString() + "\"."
			);
		}
		
		@Override
		public synchronized boolean closeChannel() throws IORuntimeException
		{
			if(!this.isChannelOpen())
			{
				return false;
			}
			
			XIO.unchecked.close(this.fileChannel);
			this.fileChannel = null;
			
			return true;
		}
		
		@Override
		public synchronized boolean isChannelOpen()
		{
			return this.fileChannel != null && this.fileChannel.isOpen();
		}
		
		@Override
		public synchronized boolean checkChannelOpen()
		{
			this.validateIsNotRetired();
			return this.isChannelOpen();
		}
		
		@Override
		public synchronized FileChannel ensureOpenChannel()
		{
			this.validateIsNotRetired();
			this.openChannel();
			
			return this.fileChannel();
		}
		
		@Override
		public synchronized boolean openChannel() throws IORuntimeException
		{
			if(this.checkChannelOpen())
			{
				return false;
			}
			
			try
			{
				this.fileChannel = XIO.openFileChannelRW(this.path);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return true;
		}
		
		@Override
		public synchronized boolean openChannel(final OpenOption... options) throws IORuntimeException
		{
			// well, the geniuses gave no means to query/check options of an existing channel
			if(this.checkChannelOpen())
			{
				return false;
			}
			
			try
			{
				this.fileChannel = XIO.openFileChannelRW(this.path, options);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return true;
		}
		
		@Override
		public synchronized boolean reopenChannel(final OpenOption... options) throws IORuntimeException
		{
			this.closeChannel();
			
			return this.openChannel(options);
		}
        
	}
	
}
