package net.jadoth.com;

import java.nio.ByteOrder;

import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.swizzling.types.SwizzleIdStrategy;

public interface ComProtocolData
{
	public String name();
	
	public String version();
	
	public ByteOrder byteOrder();
	
	public PersistenceTypeDictionaryView typeDictionary();
	
	public SwizzleIdStrategy idStrategy();
		
}
