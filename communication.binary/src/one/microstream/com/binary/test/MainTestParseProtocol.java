package one.microstream.com.binary.test;

import one.microstream.afs.nio.NioFileSystem;
import one.microstream.com.Com;
import one.microstream.com.ComFoundation;
import one.microstream.com.ComProtocol;
import one.microstream.com.ComProtocolProvider;
import one.microstream.com.ComProtocolStringConverter;
import one.microstream.com.binary.ComPersistenceAdaptorBinary;
import one.microstream.io.XIO;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.PersistenceContextDispatcher;


public class MainTestParseProtocol
{
	public static void main(final String[] args)
	{
		final BinaryPersistenceFoundation<?> pf = BinaryPersistence.Foundation()
			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.New(
				NioFileSystem.New().ensureDirectory(
					XIO.unchecked.ensureDirectory(XIO.Path("TypeDictionary"))
				)
			))
//			.setObjectIdProvider(PersistenceObjectIdProvider.Transient())
//			.setTypeIdProvider(PersistenceTypeIdProvider.Transient())
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
