package one.microstream.network.simplesession;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import one.microstream.network.exceptions.NetworkExceptionConnectionValidation;
import one.microstream.network.exceptions.NetworkExceptionTimeout;

public final class LogicSimpleAuthentication
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final int MAX_STRING_LENGTH_AUTHENTICATION = 255; // should suffice for any username+password
	private static final char AUTH_PARTS_SEPERATOR = '\n'; // no reasonable username should ever contain a newline ^^



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	public static final SimpleAuthenticationInformation readUsernamePassword(final SocketChannel channel)
		throws IOException, NetworkExceptionTimeout, NetworkExceptionConnectionValidation
	{
		final String authInfoString = LogicSimpleNetwork.readString(channel, MAX_STRING_LENGTH_AUTHENTICATION);
		final int userNameEndIndex = authInfoString.indexOf(AUTH_PARTS_SEPERATOR);
		if(userNameEndIndex < 0)
		{
			throw new NetworkExceptionConnectionValidation(channel);
		}
		return new SimpleAuthenticationInformation.Implementation(
			authInfoString.substring(0, userNameEndIndex - 1),
			authInfoString.substring(userNameEndIndex + 1)
		);
	}

	public static final void sendUsernamePassword(
		final String        username,
		final String        password,
		final SocketChannel channel
	)
		throws IOException, NetworkExceptionTimeout
	{
		LogicSimpleNetwork.sendString(username + AUTH_PARTS_SEPERATOR + password, channel);
	}


	private LogicSimpleAuthentication()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
