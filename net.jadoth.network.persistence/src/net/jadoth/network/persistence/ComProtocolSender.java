package net.jadoth.network.persistence;

import java.nio.ByteBuffer;

public interface ComProtocolSender<C>
{
	public void sendProtocol(C connection, ByteBuffer protocolBuffer);
}
