package net.jadoth.com;

public interface ComHostCreator<C>
{
	public ComHost<C> createComHost(
		int                             port                     ,
		ComConnectionListenerCreator<C> connectionListenerCreator,
		ComConnectionAcceptor<C>        connectionAcceptor
	);

	
	
	public static <C> ComHostCreator<C> New()
	{
		return new ComHostCreator.Implementation<>();
	}
	
	public final class Implementation<C> implements ComHostCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComHost<C> createComHost(
			final int                             port                     ,
			final ComConnectionListenerCreator<C> connectionListenerCreator,
			final ComConnectionAcceptor<C>        connectionAcceptor
		)
		{
			return ComHost.New(port, connectionListenerCreator, connectionAcceptor);
		}
		
	}
	
}
