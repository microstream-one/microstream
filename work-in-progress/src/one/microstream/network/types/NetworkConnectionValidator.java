package one.microstream.network.types;

import static one.microstream.X.notNull;

import java.nio.channels.SocketChannel;

import one.microstream.network.exceptions.NetworkExceptionConnectionValidation;

/**
 * Type for validating a new connection. Validation can be anything, typically a user/passwork authentication
 * and/or a host address / IP validation. Returns a result instance upon success, with "result" being anything,
 * e.g. a user instance.
 *
 * @author Thomas Muenz
 */
public interface NetworkConnectionValidator<R>
{
	public R validateConnection(SocketChannel connection) throws NetworkExceptionConnectionValidation;



	public interface Provider<R>
	{
		public NetworkConnectionValidator<R> provideValidator();

		public void disposeValidator(NetworkConnectionValidator<R> validator, Throwable cause);



		public class Trivial<R> implements NetworkConnectionValidator.Provider<R>
		{
			private final NetworkConnectionValidator<R> connectionValidator;

			public Trivial(final NetworkConnectionValidator<R> connectionValidator)
			{
				super();
				this.connectionValidator = notNull(connectionValidator);
			}

			@Override
			public NetworkConnectionValidator<R> provideValidator()
			{
				return this.connectionValidator;
			}

			@Override
			public void disposeValidator(final NetworkConnectionValidator<R> validator, final Throwable cause)
			{
				// no-op
			}

		}

	}

}
