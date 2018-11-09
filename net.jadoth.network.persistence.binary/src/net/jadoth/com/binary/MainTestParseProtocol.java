package net.jadoth.com.binary;

import java.io.File;
import java.nio.channels.SocketChannel;

import net.jadoth.com.Com;
import net.jadoth.com.ComFoundation;
import net.jadoth.com.ComProtocol;
import net.jadoth.com.ComProtocolProvider;
import net.jadoth.com.ComProtocolStringConverter;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;

public class MainTestParseProtocol
{
	public static void main(final String[] args)
	{
		final PersistenceTypeHandlerManager<?> thm = BinaryPersistence.foundation()
			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
				XFiles.ensureDirectory(new File("TypeDictionary"))
			))
			.setObjectIdProvider(SwizzleObjectIdProvider.Transient())
			.setTypeIdProvider(SwizzleTypeIdProvider.Transient())
			.getTypeHandlerManager()
			.initialize()
		;
				
		final ComFoundation<SocketChannel, ?> foundation = Com.FoundationSocketChannel()
			.setClientIdStrategy(Com.DefaultIdStrategyServer())
			.setTypeDictionary(thm.typeDictionary())
		;
		
		final ComProtocolProvider        protocolProvider = foundation.getProtocolProvider();
		final ComProtocol                protocol  = protocolProvider.provideProtocol();
		final ComProtocolStringConverter converter = foundation.getProtocolStringConverter();
		final String                     assembled = converter.assemble(protocol);
		System.out.println(assembled);
		
		final ComProtocol               parsed     = converter.parse(assembled);
		final String                    assembled2 = converter.assemble(parsed);
		System.out.println(assembled2);
	}
}
