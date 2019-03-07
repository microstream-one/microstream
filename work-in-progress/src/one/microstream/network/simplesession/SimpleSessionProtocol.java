package one.microstream.network.simplesession;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import one.microstream.exceptions.IORuntimeException;
import one.microstream.network.exceptions.NetworkExceptionConnectionValidation;
import one.microstream.network.types.NetworkClientGreeting;
import one.microstream.network.types.NetworkConnectionValidator;
import one.microstream.network.types.NetworkSessionClientGreeter;
import one.microstream.network.types.NetworkUserSession;
import one.microstream.network.types.NetworkUserSessionManager;
import one.microstream.network.types.NetworkUserSessionProtocol;

public class SimpleSessionProtocol
implements
NetworkUserSessionProtocol<SimpleSessionUser, String, SimpleSession>,
NetworkSessionClientGreeter<SimpleSession>,
NetworkUserSession.Creator<SimpleSessionUser, String, SimpleSession>,
NetworkConnectionValidator<SimpleSessionUser>,
SimpleSessionUser.Creator<SimpleSessionUser>,
NetworkClientGreeting.Creator<SimpleSession>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final String    host      = "localhost"                     ; // simple protocol doesn't forward to another host
	final int       port      = LogicSimpleNetwork.defaultPort(); // default port for simple example
	final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN            ; // network standard byte order for simple example
	final String    protocol  = "simple"                        ; // nonsense protocol name for simple example



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

//	public SimpleSessionProtocol(final String host, final int port, final ByteOrder byteOrder, final String protocol)
//	{
//		super();
//		this.host      =  notNull(host);
//		this.port      = positive(port);
//		this.byteOrder =  notNull(byteOrder);
//		this.protocol  =  notNull(protocol);
//	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected SimpleSessionUser authenticate(
		final SimpleAuthenticationInformation authInfo,
		final SocketChannel connection
	)
	{
		// trivial authentication for simple example
		if(authInfo.password().equals("secret"))
		{
			return this.createUser(authInfo);
		}
		throw new NetworkExceptionConnectionValidation(connection);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public SimpleSessionUser validateConnection(final SocketChannel connection)
		throws NetworkExceptionConnectionValidation
	{
		try
		{
			return this.authenticate(
				LogicSimpleAuthentication.readUsernamePassword(connection),
				connection
			);
		}
		catch(final IOException e)
		{
			// checked exceptions and functional programming / clean architecture just don't get along with each other
			throw new IORuntimeException(e);
		}
	}

	@Override
	public SimpleSessionUser createUser(final SimpleAuthenticationInformation parameter)
	{
		return new SimpleSessionUser.Implementation(parameter.username());
	}

	@Override
	public SimpleSession createUserSession(
		final NetworkUserSessionManager<SimpleSessionUser, SimpleSession> sessionManager,
		final SimpleSessionUser user,
		final SocketChannel connection
	)
	{
		// insecure simplistic session id
		return new SimpleSession(sessionManager, user, connection, System.nanoTime());
	}

	@Override
	public void greetClient(final SimpleSession session)
	{
		final NetworkClientGreeting greeting = this.createGreeting(session);
		try
		{
			LogicSimpleNetwork.sendString(greeting.toString(), session.channel());
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}

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

	@Override
	public NetworkUserSession.Creator<SimpleSessionUser, ?, SimpleSession> provideUserSessionCreator()
	{
		return this; // simple immutable protocol can handle session protocol per-thread tasks by itself
	}

	@Override
	public NetworkConnectionValidator<SimpleSessionUser> provideValidator()
	{
		return this; // simple immutable protocol can handle all session protocol per-thread tasks by itself
	}

	@Override
	public void disposeValidator(final NetworkConnectionValidator<SimpleSessionUser> validator, final Throwable cause)
	{
		// no-op
	}

	@Override
	public NetworkSessionClientGreeter<SimpleSession> provideClientGreeter()
	{
		return this; // simple immutable protocol can handle session protocol per-thread tasks by itself
	}

	@Override
	public void disposeClientGreeter(
		final NetworkSessionClientGreeter<SimpleSession> clientGreeter,
		final Throwable                                  cause
	)
	{
		// no-op
	}

}
