package net.jadoth.com;


/**
 * Meta type to allow wrapping of connection handling logic types with additional aspects like
 * authentication, encryption and the like.
 * 
 * @author TM
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
		return new ComConnectionLogicDispatcher.Implementation<>();
	}
	
	public final class Implementation<C> implements ComConnectionLogicDispatcher<C>
	{
		Implementation()
		{
			super();
		}
		
	}
	
}
