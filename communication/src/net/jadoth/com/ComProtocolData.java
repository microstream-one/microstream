package net.jadoth.com;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceIdStrategy;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;

public interface ComProtocolData extends PersistenceTypeDictionaryViewProvider
{
	public String name();
	
	public String version();
	
	public ByteOrder byteOrder();
	
	public PersistenceTypeDictionaryView typeDictionary();
	
	public PersistenceIdStrategy idStrategy();
	
	@Override
	public default PersistenceTypeDictionaryView provideTypeDictionary()
	{
		return this.typeDictionary();
	}
	
}
