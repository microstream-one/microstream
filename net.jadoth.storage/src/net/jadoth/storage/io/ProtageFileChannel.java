package net.jadoth.storage.io;

import static net.jadoth.X.notNull;

import java.lang.ref.WeakReference;

public interface ProtageFileChannel
{
	public ProtageFile file();
	
	/**
	 * A reference to the exlcusive owner {@link Thread} of this channel.
	 * If the reference returns <code>null</code>, the owner {@link Thread} of this channel has already
	 * ceased to exist, making this channel an orphan that should be closed and dereferenced, as well.
	 * If the reference ({@link Owner} instance) itself is <code>null</code>, then this channel has not
	 * been created for an exclusive owner in the first place but might be intended to be passed around
	 * between threads.
	 * 
	 * @return a reference to the exlcusive owner {@link Thread}, if applicable.
	 */
	public Owner owner();
	
	
	
	public static Owner Owner(final Thread ownerThread)
	{
		return new Owner.Implementation(
			notNull(ownerThread)
		);
	}
	
	public interface Owner
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		public Thread get();
		
	
		
		public final class Implementation extends WeakReference<Thread> implements Owner
		{
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation(final Thread referent)
			{
				super(referent);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final Thread get()
			{
				return super.get();
			}
			
		}
		
	}
	
}
