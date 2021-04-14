package one.microstream.afs.blobstore.types;

import static one.microstream.X.notNull;

import one.microstream.afs.exceptions.AfsExceptionRetiredFile;
import one.microstream.afs.types.AFile;
import one.microstream.chars.XChars;

public interface BlobStoreFileWrapper extends AFile.Wrapper, BlobStoreItemWrapper
{
	public boolean retire();

	public boolean isRetired();

	public boolean isHandleOpen();

	public boolean checkHandleOpen();

	public boolean openHandle();

	public boolean closeHandle();

	public BlobStoreFileWrapper ensureOpenHandle();


	public abstract class Abstract<U> extends AFile.Wrapper.Abstract<U> implements BlobStoreFileWrapper
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private BlobStorePath  path              ;
		private boolean        handleOpen = false;



        ///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

        protected Abstract(
			final AFile         actual,
			final U             user  ,
			final BlobStorePath path
        )
		{
			super(actual, user);
			this.path = notNull(path);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public BlobStorePath path()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				return this.path;
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
				"File is retired: " + XChars.systemString(this) + "(\"" + this.toPathString() + "\"."
			);
		}

		@Override
		public boolean isHandleOpen()
		{
			synchronized(this.mutex())
			{
				return this.handleOpen;
			}
		}

		@Override
		public boolean checkHandleOpen()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				return this.isHandleOpen();
			}
		}

		@Override
		public boolean openHandle()
		{
			synchronized(this.mutex())
			{
				if(this.checkHandleOpen())
				{
					return false;
				}
	
				this.handleOpen = true;
				return true;
			}
		}

		@Override
		public boolean closeHandle()
		{
			synchronized(this.mutex())
			{
				if(!this.isHandleOpen())
				{
					return false;
				}
	
				this.handleOpen = false;
	
				return true;
			}
		}

		@Override
		public BlobStoreFileWrapper ensureOpenHandle()
		{
			synchronized(this.mutex())
			{
				this.validateIsNotRetired();
				this.openHandle();
	
				return this;
			}
		}

	}

}
