package one.microstream.afs.blobstore;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;
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
			return this.path;
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
		public synchronized boolean isHandleOpen()
		{
			return this.handleOpen;
		}

		@Override
		public synchronized boolean checkHandleOpen()
		{
			this.validateIsNotRetired();
			return this.isHandleOpen();
		}

		@Override
		public synchronized boolean openHandle()
		{
			if(this.checkHandleOpen())
			{
				return false;
			}

			this.handleOpen = true;
			return true;
		}

		@Override
		public boolean closeHandle()
		{
			if(!this.isHandleOpen())
			{
				return false;
			}

			this.handleOpen = false;

			return true;
		}

		@Override
		public BlobStoreFileWrapper ensureOpenHandle()
		{
			this.validateIsNotRetired();
			this.openHandle();

			return this;
		}

	}

}
