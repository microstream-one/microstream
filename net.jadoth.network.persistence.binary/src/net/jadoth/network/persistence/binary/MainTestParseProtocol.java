package net.jadoth.network.persistence.binary;

import java.io.File;

import net.jadoth.network.persistence.Com;
import net.jadoth.network.persistence.ComFoundation;
import net.jadoth.network.persistence.ComProtocol;
import net.jadoth.network.persistence.ComProtocolStringConverter;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.PersistenceTypeDictionary;

public class MainTestParseProtocol
{
	public static void main(final String[] args)
	{
		final PersistenceTypeDictionary td = BinaryPersistence.foundation()
			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(new File("TypeDictionary")))
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
		
		System.out.println(assembled);
		
		final ComProtocol                parsed     = converter.parse(assembled);
		System.out.println(converter.assemble(parsed));
	}
}
