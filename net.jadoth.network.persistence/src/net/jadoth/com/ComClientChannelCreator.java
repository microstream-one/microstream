package net.jadoth.com;

import net.jadoth.persistence.types.PersistenceFoundation;

public interface ComClientChannelCreator<C>
{
	public ComClientChannel createChannel(
		final C           connection,
		final ComProtocol protocol  ,
		final ComClient   parent
	);
	
	
	
	public static <C> ComClientChannelCreator<C> New()
	{
		return new ComClientChannelCreator.Implementation<>();
	}
	
	public class Abstract<C> implements ComClientChannelCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceFoundation<?, ?> persistenceFoundation;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Abstract(final PersistenceFoundation<?, ?> persistenceFoundation)
		{
			super();
			this.persistenceFoundation = persistenceFoundation;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComClientChannel createChannel(
			final C           connection,
			final ComProtocol protocol  ,
			final ComClient   parent
		)
		{
			return ComClientChannel.New();
		}
		
	}
		
}
