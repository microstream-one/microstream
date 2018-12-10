package net.jadoth.com.binary.test;

import java.io.File;

import net.jadoth.com.Com;
import net.jadoth.com.ComFoundation;
import net.jadoth.com.ComProtocol;
import net.jadoth.com.ComProtocolProvider;
import net.jadoth.com.ComProtocolStringConverter;
import net.jadoth.com.binary.ComPersistenceAdaptorBinary;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.PersistenceContextDispatcher;
import net.jadoth.persistence.types.PersistenceObjectIdProvider;
import net.jadoth.persistence.types.PersistenceTypeIdProvider;


public class MainTestParseProtocol
{
	public static void main(final String[] args)
	{
		final BinaryPersistenceFoundation<?> pf = BinaryPersistence.Foundation()
			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
				XFiles.ensureDirectory(new File("TypeDictionary"))
			))
			.setObjectIdProvider(PersistenceObjectIdProvider.Transient())
			.setTypeIdProvider(PersistenceTypeIdProvider.Transient())
			.setContextDispatcher(
				PersistenceContextDispatcher.LocalObjectRegistration()
			)
		;
				
		final ComFoundation.Default<?> foundation = Com.Foundation()
			.setPersistenceAdaptorCreator(ComPersistenceAdaptorBinary.Creator(pf))
		;
		
		final ComProtocolProvider<?>     protocolProvider = foundation.getProtocolProvider();
		final ComProtocol                protocol         = protocolProvider.provideProtocol(null);
		final ComProtocolStringConverter converter        = foundation.getProtocolStringConverter();
		final String                     assembled        = converter.assemble(protocol);
		System.out.println(assembled);
		
		final ComProtocol parsed     = converter.parse(assembled);
		final String      assembled2 = converter.assemble(parsed);
		System.out.println(assembled2);
	}
	
}
