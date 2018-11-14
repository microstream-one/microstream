package net.jadoth.com;

public interface ComClientChannelCreator<C>
{
	public ComClientChannel createChannel(C connection);
	
	
	
	public static <C> ComClientChannelCreator<C> New()
	{
		return new ComClientChannelCreator.Implementation<>();
	}
	
	public final class Implementation<C> implements ComClientChannelCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
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
		public ComClientChannel createChannel(final C connection)
		{
			return ComClientChannel.New();
		}
		
	}
		
}
