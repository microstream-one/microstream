package net.jadoth.com.binary.test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.jadoth.com.Com;
import net.jadoth.com.ComHost;
import net.jadoth.com.ComHostChannel;
import net.jadoth.com.XSockets;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeIdProvider;

public class MainTestComServer
{
	static final BinaryPersistenceFoundation<?> pf = BinaryPersistence.foundation()
		.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
			XFiles.ensureDirectory(new File("TypeDictionary"))
		))
		.setObjectIdProvider(SwizzleObjectIdProvider.Transient())
		.setTypeIdProvider(SwizzleTypeIdProvider.Transient())
	;
	
	// (16.11.2018 TM)TODO: Convenience host methods
	// (16.11.2018 TM)FIXME: default host persistence plus persistable type iterable
	static final ComHost<?> COM = Com.Foundation()
		.setHostBindingAddress(XSockets.localHostSocketAddress(1337))
		.setHostChannelAcceptor(MainTestComServer::handleChannel)
		.setPersistence(pf)
		.createHost()
	;
	
	public static void main(final String[] args)
	{
		COM.run();
	}
	
	static void handleChannel(final ComHostChannel<SocketChannel> channel)
	{
		final Object received = channel.receive();
		
		try
		{
			System.out.println("Received from " + channel.connection().getRemoteAddress()+": " + received);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}
		
		final String answer = "You said: \"" + received + "\"";
		channel.send(answer);
		channel.close();
	}
	
}
