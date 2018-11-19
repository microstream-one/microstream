package net.jadoth.com.binary.test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.jadoth.com.Com;
import net.jadoth.com.ComException;
import net.jadoth.com.ComHostChannel;
import net.jadoth.com.binary.ComPersistenceAdaptorBinary;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;

public class MainTestComServer
{
	public static void main(final String[] args)
	{
		// (18.11.2018 TM)FIXME: default PersistenceTypeDictionaryViewProvider with explicitely defined type list
		// (18.11.2018 TM)FIXME: default SwizzleIdStrategy

		// (16.11.2018 TM)TODO: Convenience host methods
		final BinaryPersistenceFoundation<?> persistence = BinaryPersistence.Foundation()
			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
				XFiles.ensureDirectory(new File("TypeDictionary"))
			))
			.setObjectIdProvider(SwizzleObjectIdProvider.Transient())
			.setTypeIdProvider(SwizzleTypeIdProvider.Transient())
		;
		
		// convenience & customization example 1
//		final ComHost<?> host = Com.Foundation()
//			.setPersistenceAdaptor(ComPersistenceAdaptorBinary.New(persistence))
//			.setHostBindingAddress(Com.localHostSocketAddress(Com.defaultPort()))
//			.setHostChannelAcceptor(Com::bounce)
//			.createHost()
//		;
//		System.out.println("Starting host ...");
//		host.run();
		
		// convenience & customization example 2
//		final ComHost<?> host = Com.Host(
//			ComPersistenceAdaptorBinary.New(persistence)
//		);
//		System.out.println("Starting host ...");
//		host.run();

		// convenience & customization example 3
//		Com.runHost(ComPersistenceAdaptorBinary.New(persistence), channel -> channel.send("Go away."));
		
		// convenience & customization example 4
//		Com.runHost(ComPersistenceAdaptorBinary.New(persistence), MainTestComServer::logAndBounce);
		
		// convenience & customization example 5
		Com.runHost(ComPersistenceAdaptorBinary.New(persistence));
	}
	
	/**
	 * Slightly improved version of {@link Com#bounce(ComHostChannel)}
	 * @param channel
	 */
	public static void logAndBounce(final ComHostChannel<SocketChannel> channel)
	{
		final Object received = channel.receive();
		final String string = received.toString();
		
		try
		{
			System.out.println("Received from " + channel.connection().getRemoteAddress()+": " + string);
		}
		catch(final IOException e)
		{
			throw new ComException(e);
		}
		
		final String answer = "You said: \"" + string + "\"";
		channel.send(answer);
		channel.close();
	}
	
}
