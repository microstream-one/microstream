package one.microstream.storage.types;

import java.util.function.Consumer;

import one.microstream.afs.AFile;
import one.microstream.collections.XArrays;
import one.microstream.storage.exceptions.StorageException;

public interface StorageLiveFile<S extends StorageLiveFile<S>> extends StorageClosableFile
{
	public boolean hasUsers();
	
	public boolean executeIfUnsued(Consumer<? super S> action);
	
	public boolean registerUsage(StorageFileUser fileUser);
	
	public boolean clearUsages(StorageFileUser fileUser);
	
	public boolean unregisterUsage(StorageFileUser fileUser);
		
	public boolean unregisterUsageClosing(
		StorageFileUser     fileUser     ,
		Consumer<? super S> closingAction
	);
	
	
	public abstract class Abstract<S extends StorageLiveFile<S>>
	extends StorageFile.Abstract
	implements StorageLiveFile<S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		/*
		 * note: for very short arrays (~ 5 references), arrays are faster than hash tables.
		 * The planned/expected user count for storage files should be something around 2-3.
		 */
		private Usage[] usages     = null;
		private int     usagesSize = 0;
		
		static final class Usage
		{
			StorageFileUser user ;
			int             count;
			
			Usage(final StorageFileUser user)
			{
				super();
				this.user = user;
			}
			
			final boolean increment()
			{
				return ++this.count == 1;
			}
			
			final boolean decrement()
			{
				return --this.count == 0;
			}
			
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final AFile file)
		{
			super(file);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@SuppressWarnings("unchecked") // safe by design: S is Self-Type.
		protected S $()
		{
			return (S)this;
		}
		
		@Override
		public final synchronized boolean hasUsers()
		{
			return this.usagesSize != 0;
		}
		
		@Override
		public final synchronized boolean executeIfUnsued(
			final Consumer<? super S> action
		)
		{
			if(this.hasUsers())
			{
				return false;
			}
			
			action.accept(this.$());
			
			return true;
		}

		@Override
		public final synchronized boolean registerUsage(final StorageFileUser fileUser)
		{
			return this.ensureEntry(fileUser).increment();
		}
		
		private Usage ensureEntry(final StorageFileUser fileUser)
		{
			// usages NPE prevented by usagesSize == 0.
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					return this.usages[i];
				}
			}
			
			if(this.usages == null)
			{
				this.usages = new Usage[1];
			}
			else
			{
				if(this.usagesSize >= this.usages.length)
				{
					this.usages = XArrays.enlarge(this.usages, this.usages.length * 2);
				}
			}
			
			return this.usages[this.usagesSize++] = new Usage(fileUser);
		}
		
		private void checkForUsagesArrayClearing()
		{
			if(this.usagesSize == 0)
			{
				this.usages = null;
			}
		}
		
		@Override
		public final synchronized boolean clearUsages(final StorageFileUser fileUser)
		{
			// usages NPE prevented by usagesSize == 0.
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					XArrays.removeFromIndex(this.usages, this.usagesSize--, i);
					this.checkForUsagesArrayClearing();
					
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public final synchronized boolean unregisterUsage(final StorageFileUser fileUser)
		{
			// usages NPE prevented by usagesSize == 0.
			for(int i = 0; i < this.usagesSize; i++)
			{
				if(this.usages[i].user == fileUser)
				{
					if(this.usages[i].decrement())
					{
						XArrays.removeFromIndex(this.usages, this.usagesSize--, i);
						this.checkForUsagesArrayClearing();
						
						return true;
					}
					
					return false;
				}
			}
			
			// (29.11.2019 TM)EXCP: proper exception
			throw new StorageException(StorageFileUser.class.getSimpleName() + " not found " + fileUser);
		}
				
		@Override
		public boolean unregisterUsageClosing(
			final StorageFileUser     fileUser     ,
			final Consumer<? super S> closingAction
		)
		{
			if(this.unregisterUsage(fileUser) && !this.hasUsers())
			{
				if(closingAction != null)
				{
					closingAction.accept(this.$());
				}
				
				this.close();
				return true;
			}
			
			return false;
		}
				
	}
}
