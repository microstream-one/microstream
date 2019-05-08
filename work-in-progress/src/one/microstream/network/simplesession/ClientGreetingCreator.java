package one.microstream.network.simplesession;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import java.nio.ByteOrder;

import one.microstream.network.types.NetworkClientGreeting;

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
		return new NetworkClientGreeting.Default(
			this.host,
			this.port,
			this.byteOrder,
			session.sessionId(),
			this.protocol
		);
	}

}
