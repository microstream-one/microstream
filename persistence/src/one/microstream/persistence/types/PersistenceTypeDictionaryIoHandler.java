package one.microstream.persistence.types;

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
		
	}
		
}
