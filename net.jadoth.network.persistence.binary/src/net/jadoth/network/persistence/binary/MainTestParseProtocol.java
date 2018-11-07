package net.jadoth.network.persistence.binary;

import java.io.File;

import net.jadoth.files.XFiles;
import net.jadoth.network.persistence.Com;
import net.jadoth.network.persistence.ComFoundation;
import net.jadoth.network.persistence.ComProtocol;
import net.jadoth.network.persistence.ComProtocolStringConverter;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
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
		final PersistenceTypeDictionary td = thm.typeDictionary();
		
//		final PersistenceTypeDictionaryManager tdm = BinaryPersistence.foundation()
//			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
//				XFiles.ensureDirectory(new File("TypeDictionary"))
//			))
//			.getTypeDictionaryManager()
//		;
//		final PersistenceTypeDictionary td = tdm.provideTypeDictionary();
//		if(td.isEmpty())
//		{
//			tdm.registerRuntimeTypeDefinitions(BinaryPersistence.nativeHandlers());
//		}
		
		
//		BinaryPersistence.foundation()
//			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
//				XFiles.ensureDirectory(new File("TypeDictionary")))
//			)
//			.setObjectIdProvider(SwizzleObjectIdProvider.Transient())
//			.setTypeIdProvider(SwizzleTypeIdProvider.Transient())
//			.createPersistenceManager()
//			.typeDictionary()
//		;
		
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
