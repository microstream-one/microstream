package net.jadoth.persistence.types;

public interface PersistenceTypeDictionaryIoHandler
extends PersistenceTypeDictionaryLoader, PersistenceTypeDictionaryStorer
{
	// just a typing interface so far
	
	public interface Provider
	{
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler();
		
		public final class Implementation implements PersistenceTypeDictionaryIoHandler.Provider
		{

			@Override
			public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler()
			{
				throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDictionaryIoHandler.Provider#provideTypeDictionaryIoHandler()
			}
			
		}
	}
}
