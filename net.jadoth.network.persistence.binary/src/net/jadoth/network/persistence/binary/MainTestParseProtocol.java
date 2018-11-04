package net.jadoth.network.persistence.binary;

import net.jadoth.network.persistence.Com;
import net.jadoth.network.persistence.ComFoundation;
import net.jadoth.network.persistence.ComProtocol;
import net.jadoth.network.persistence.ComProtocolStringConverter;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDictionary;

public class MainTestParseProtocol
{
	public static void main(final String[] args)
	{
		final PersistenceTypeDictionary td = BinaryPersistence.foundation()
			.getTypeDictionaryManager()
			.provideTypeDictionary()
		;
		
		final ComFoundation<?> foundation = Com.Foundation()
			.setIdStrategy(Com.DefaultIdStrategyServer())
			.setTypeDictionary(td)
		;
		final ComProtocol                protocol   = foundation.getProtocol();
		final ComProtocolStringConverter converter  = foundation.getProtocolStringConverter();
		final String                     assembled  = converter.assemble(protocol);
		final ComProtocol                parsed     = converter.parse(assembled);
		System.out.println(converter.assemble(parsed));
	}
}
