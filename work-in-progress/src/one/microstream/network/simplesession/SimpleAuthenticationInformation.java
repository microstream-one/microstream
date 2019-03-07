package one.microstream.network.simplesession;

public interface SimpleAuthenticationInformation
{
	public String username();

	public String password();



	public class Implementation implements SimpleAuthenticationInformation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final String username;
		private final String password;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final String username, final String password)
		{
			super();
			this.username = username;
			this.password = password;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String username()
		{
			return this.username;
		}

		@Override
		public String password()
		{
			return this.password;
		}

	}

}
