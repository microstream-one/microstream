package one.microstream.persistence.types;

import one.microstream.afs.types.AFile;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;

public interface PersistenceTypeDictionaryIoHandler
extends PersistenceTypeDictionaryLoader, PersistenceTypeDictionaryStorer
{
	// just a typing interface so far
		
	public interface Provider
	{
		public default PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler()
		{
			return this.provideTypeDictionaryIoHandler(null);
		}
		
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
			PersistenceTypeDictionaryStorer writeListener
		);
		
		
		public abstract class Abstract implements PersistenceTypeDictionaryIoHandler.Provider
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator)
			{
				super();
				this.fileHandlerCreator = fileHandlerCreator;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			protected abstract AFile defineTypeDictionaryFile();

			@Override
			public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
				final PersistenceTypeDictionaryStorer writeListener
			)
			{
				/*
				 * (04.03.2019 TM)TODO: forced delegating API is not a clean solution.
				 * This is only a temporary solution. See the task containing "PersistenceDataFile".
				 */
				final AFile file = this.defineTypeDictionaryFile();
				
				return this.fileHandlerCreator.createTypeDictionaryIoHandler(file, writeListener);
			}
			
		}
		
	}
		
}
