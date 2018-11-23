package net.jadoth.com.binary.test;

import java.io.File;
import java.nio.channels.SocketChannel;

import net.jadoth.com.ComChannel;
import net.jadoth.com.ComDefaultIdStrategy;
import net.jadoth.com.binary.ComPersistenceChannelBinary;
import net.jadoth.files.XFiles;
import net.jadoth.meta.XDebug;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.internal.CompositeSwizzleIdProvider;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.util.BufferSizeProvider;

public class UtilTestOldCom
{
	public static int defaultPort()
	{
		return 1337;
	}
	
	public static File defaultSystemDirectory()
	{
		return new File("networkpersistencedemo");
	}
	
	
	public static ComChannel openComChannel(
		final SocketChannel socketChannel  ,
		final File          systemDirectory,
		final boolean       isClient
	)
	{
		final BinaryPersistenceFoundation<?> foundation = createFoundation(systemDirectory, isClient);
		
		final ComPersistenceChannelBinary.Default channel = ComPersistenceChannelBinary.New(
			socketChannel,
			BufferSizeProvider.New()
		);
		foundation.setPersistenceChannel(channel);
		
		final PersistenceManager<Binary> pm = foundation.createPersistenceManager();
				
		return ComChannel.New(pm);
	}
		
	private static BinaryPersistenceFoundation<?> createFoundation(
		final File    systemDirectory,
		final boolean isClient
	)
	{
		XFiles.ensureDirectory(systemDirectory);
		
		// (13.08.2018 TM)NOTE: copied from EmbeddedStorage#createConnectionFoundation
		
		final PersistenceTypeDictionaryFileHandler dictionaryStorage = PersistenceTypeDictionaryFileHandler.NewInDirecoty(
			systemDirectory
		);

		// (17.08.2018 TM)NOTE: use once to create a TypeDictionary, then switch back to Failing implementation
//		final FileTypeIdProvider fileTypeIdProvider = new FileTypeIdProvider(
//			new File(systemDirectory, Persistence.defaultFilenameTypeId())
//		);

		final ComDefaultIdStrategy idStrategy = ComDefaultIdStrategy.New(
			isClient
			? 9_200_000_000_000_000_000L // temporary id range.
			: 9_100_000_000_000_000_000L // temporary id range
		);
		XDebug.println("Starting OID: " + idStrategy.startingObjectId());
		
		final CompositeSwizzleIdProvider idProvider = idStrategy
			.createIdProvider()
			.initialize()
		;
		
		return BinaryPersistenceFoundation.New()
			.setTypeDictionaryIoHandling   (dictionaryStorage)
			.setSwizzleIdProvider          (idProvider       )
			
			// (17.11.2018 TM)NOTE: these are default anyway.
//			.setTypeEvaluatorPersistable   (Persistence::isPersistable   )
//			.setTypeEvaluatorTypeIdMappable(Persistence::isTypeIdMappable)
		;
	}
	
	
}
