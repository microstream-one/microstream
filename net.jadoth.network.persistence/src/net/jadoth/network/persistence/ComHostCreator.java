package net.jadoth.network.persistence;

public interface ComHostCreator
{
	public ComHost createComHost(int port, ComConnectionAcceptor connectionAcceptor);

	
	
	public static ComHostCreator Creator()
	{
		return new ComHostCreator.Implementation();
	}
	
	public final class Implementation implements ComHostCreator
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
		public final ComHost createComHost(final int port, final ComConnectionAcceptor connectionAcceptor)
		{
			return ComHost.New(port, connectionAcceptor);
		}
		
	}
	
}
