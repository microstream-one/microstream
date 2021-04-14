package one.microstream.communication.types;


/**
 * Meta type to allow wrapping of connection handling logic types with additional aspects like
 * authentication, encryption and the like.
 * 
 * 
 *
 * @param <C>
 */
public interface ComConnectionLogicDispatcher<C>
{
	public default ComConnectionAcceptorCreator<C> dispatch(final ComConnectionAcceptorCreator<C> creator)
	{
		// no-op by default
		return creator;
	}
	
	public default ComConnectionHandler<C> dispatch(final ComConnectionHandler<C> connectionHandler)
	{
		// no-op by default
		return connectionHandler;
	}
	
	
	
	public static <C> ComConnectionLogicDispatcher<C> New()
	{
		return new ComConnectionLogicDispatcher.Default<>();
	}
	
	public final class Default<C> implements ComConnectionLogicDispatcher<C>
	{
		Default()
		{
			super();
		}
		
	}
	
}
