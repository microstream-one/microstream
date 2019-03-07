package one.microstream.network.simplesession;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import one.microstream.exceptions.IORuntimeException;
import one.microstream.network.exceptions.NetworkExceptionConnectionValidation;
import one.microstream.network.types.NetworkConnectionValidator;

public class SimpleSessionAuthenticator<U extends SimpleSessionUser> implements NetworkConnectionValidator<U>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final SimpleSessionUser.Creator<U> userCreator;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SimpleSessionAuthenticator(final SimpleSessionUser.Creator<U> userCreator)
	{
		super();
		this.userCreator = notNull(userCreator);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public U validateConnection(final SocketChannel connection) throws NetworkExceptionConnectionValidation
	{
		try
		{
			final SimpleAuthenticationInformation authInfo = LogicSimpleAuthentication.readUsernamePassword(connection);
			// trivial authentication for simple example
			if(!authInfo.password().equals("secret"))
			{
				throw new NetworkExceptionConnectionValidation(connection);
			}
			return this.userCreator.createUser(authInfo);
		}
		catch(final IOException e)
		{
			// checked exceptions and functional programming / clean architecture just don't get along with each other
			throw new IORuntimeException(e);
		}

	}

}
