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
	
	public FileChannel ensureOpenChannel(OpenOption... options);
		
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
			this.ensurePositionAtFileEnd();
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
			
			this.ensureClearedFileChannelField();
			
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
			
			// see inside for implicit append mode. Crazy stuff.
			this.openChannel();
			
			return this.fileChannel();
		}
		

		@Override
		public synchronized FileChannel ensureOpenChannel(final OpenOption... options)
		{
			this.validateIsNotRetired();
			this.openChannel(options);
			
			return this.fileChannel();
		}
		
		@Override
		public synchronized boolean openChannel() throws IORuntimeException
		{
			// reroute to open options variant to reuse its position setting logic
			return this.openChannel(new OpenOption[0]);
		}
		
		@Override
		public synchronized boolean openChannel(final OpenOption... options) throws IORuntimeException
		{
			// well, the geniuses provided no means to query/check the creation options of an existing channel
			if(this.checkChannelOpen())
			{
				return false;
			}
			
			try
			{
				final FileChannel fileChannel = XIO.openFileChannelRW(this.path, options);
				this.internalSetFileChannel(fileChannel);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
			
			return true;
		}
		
		protected void internalSetFileChannel(final FileChannel fileChannel)
		{
			this.fileChannel = fileChannel;
			
			/*
			 * The channel is set implicitely to the end since append mode is somehow never usable
			 * Details:
			 * When using RandomAccessFile, there is no way to make it create the channel in append mode
			 * When creating the channel by using the open options, then READ and APPEND have an
			 * inexplicably stupid conflict that causes an exception.
			 * So the only viable option to get an always-append file channel is to NOT use append mode,
			 * set the position to size and never change it again.
			 * Typical JDK moronity.
			 */
			this.ensurePositionAtFileEnd();
		}
		
		protected void ensurePositionAtFileEnd() throws IORuntimeException
		{
			if(this.fileChannel == null)
			{
				return;
			}
			
			try
			{
				// explicit check might increase IO efficiency
				final long fileSize = this.fileChannel.size();
				if(this.fileChannel.position() != fileSize)
				{
					this.fileChannel.position(fileSize);
				}
			}
			catch(final IOException e)
			{
				// reset field in case the position setting caused the exception
				this.ensureClearedFileChannelField(e);
				
				throw new IORuntimeException(e);
			}
		}
		
		private void ensureClearedFileChannelField()
		{
			this.ensureClearedFileChannelField(null);
		}
		
		private void ensureClearedFileChannelField(final Throwable cause)
		{
			final FileChannel fc = this.fileChannel;
			this.fileChannel = null;
			XIO.unchecked.close(fc, cause);
		}
				
		@Override
		public synchronized boolean reopenChannel(final OpenOption... options) throws IORuntimeException
		{
			this.closeChannel();
			
			return this.openChannel(options);
		}
        
	}
	
}
