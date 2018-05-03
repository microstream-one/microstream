package net.jadoth.network.simplesession;

import static net.jadoth.X.notNull;
import static net.jadoth.math.JadothMath.positive;

import java.nio.ByteOrder;

import net.jadoth.network.types.NetworkClientGreeting;

public class ClientGreetingCreator implements NetworkClientGreeting.Creator<SimpleSession>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final String    host     ;
	private final int       port     ;
	private final ByteOrder byteOrder;
	private final String    protocol ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ClientGreetingCreator(final String host, final int port, final ByteOrder byteOrder, final String protocol)
	{
		super();
		this.host      =  notNull(host);
		this.port      = positive(port);
		this.byteOrder =  notNull(byteOrder);
		this.protocol  =  notNull(protocol);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public NetworkClientGreeting createGreeting(final SimpleSession session)
	{
		return new NetworkClientGreeting.Implementation(
			this.host,
			this.port,
			this.byteOrder,
			session.sessionId(),
			this.protocol
		);
	}

}
