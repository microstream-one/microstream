package one.microstream.afs.nio.types;

/*-
 * #%L
 * microstream-afs-nio
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import one.microstream.afs.exceptions.AfsExceptionRetiredFile;
import one.microstream.afs.types.AFile;
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
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				return this.path;
			}
		}
		
		@Override
		public FileChannel fileChannel()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				return this.fileChannel;
			}
		}
		
		@Override
		public boolean retire()
		{
			synchronized(this.mutex())
			{
				if(this.path == null)
				{
					return false;
				}
				
				this.path = null;
				
				return true;
			}
		}
		
		@Override
		public boolean isRetired()
		{
			synchronized(this.mutex())
			{
				return this.path == null;
			}
		}
		
		public void validateIsNotRetired()
		{
			if(!this.isRetired())
			{
				return;
			}
			
			throw new AfsExceptionRetiredFile(
				"File is retired: " + XChars.systemString(this) + "(\"" + this.toPathString() + "\")."
			);
		}
		
		@Override
		public boolean closeChannel() throws IORuntimeException
		{
			synchronized(this.mutex())
			{
				if(!this.isChannelOpen())
				{
					return false;
				}
				
				this.ensureClearedFileChannelField();
				
				return true;
			}
		}
		
		@Override
		public boolean isChannelOpen()
		{
			synchronized(this.mutex())
			{
				return this.fileChannel != null && this.fileChannel.isOpen();
			}
		}
		
		@Override
		public boolean checkChannelOpen()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				return this.isChannelOpen();
			}
		}
		
		@Override
		public FileChannel ensureOpenChannel()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				
				// see inside for implicit append mode. Crazy stuff.
				this.openChannel(this.normalizeOpenOptions());
				
				return this.fileChannel();
			}
		}
		
		private static final OpenOption[] EMPTY_OPEN_OPTIONS = new OpenOption[0];
		
		protected OpenOption[] normalizeOpenOptions(final OpenOption... options)
		{
			if(options == null)
			{
				return EMPTY_OPEN_OPTIONS;
			}
			
			this.validateOpenOptions(options);
			
			return options;
		}
		
		protected abstract void validateOpenOptions(OpenOption... options);
		

		@Override
		public FileChannel ensureOpenChannel(final OpenOption... options)
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				this.openChannel(options);
				
				return this.fileChannel();
			}
		}
		
		@Override
		public boolean openChannel() throws IORuntimeException
		{
			synchronized(this.mutex())
			{
				// reroute to open options variant to reuse its position setting logic
				return this.openChannel((OpenOption[])null);
			}
		}
		
		@Override
		public boolean openChannel(final OpenOption... options) throws IORuntimeException
		{
			synchronized(this.mutex())
			{
				// well, the geniuses provided no means to query/check the creation options of an existing channel
				if(this.checkChannelOpen())
				{
					return false;
				}
				
				final OpenOption[] effectiveOptions = this.normalizeOpenOptions(options);
				
				try
				{
					// READ / WRITE are defined by #normalizeOpenOptions depending on the specific class
					final FileChannel fileChannel = XIO.openFileChannel(this.path, effectiveOptions);
					this.internalSetFileChannel(fileChannel);
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}
				
				return true;
			}
		}
		
		protected void internalSetFileChannel(final FileChannel fileChannel)
		{
			this.fileChannel = fileChannel;
			
			/*
			 * The channel is set implicitly to the end since append mode is somehow never usable
			 * When using RandomAccessFile, there is no way to make it create the channel in append mode
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
		public boolean reopenChannel(final OpenOption... options) throws IORuntimeException
		{
			synchronized(this.mutex())
			{
				this.closeChannel();
				
				return this.openChannel(options);
			}
		}
        
	}
	
}
