package net.jadoth.com.binary.test;

import java.io.File;

import net.jadoth.com.Com;
import net.jadoth.com.ComFoundation;
import net.jadoth.com.ComHostContext;
import net.jadoth.com.ComProtocol;
import net.jadoth.com.ComProtocolProvider;
import net.jadoth.com.ComProtocolStringConverter;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;


public class MainTestParseProtocol
{
	public static void main(final String[] args)
	{
		final BinaryPersistenceFoundation<?> pf = BinaryPersistence.foundation()
			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
				XFiles.ensureDirectory(new File("TypeDictionary"))
			))
			.setObjectIdProvider(SwizzleObjectIdProvider.Transient())
			.setTypeIdProvider(SwizzleTypeIdProvider.Transient())
		;
		
		final PersistenceManager<?> pm;
		
				
		final ComFoundation.Default<?> foundation = Com.Foundation()
			.setClientIdStrategy(Com.DefaultIdStrategyServer())
//			.setPersistenceAdaptor(ComPersistenceAdaptor.New(pf))
			.setHostContext(ComHostContext.Builder()
				.setPersistence(pf)
				.setChannelAcceptorLogic(System.out::println)
				.buildHostContext()
			)
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
