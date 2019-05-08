package one.microstream.network.simplesession;

import one.microstream.typing.Named;

public interface SimpleSessionUser extends Named
{
	@Override
	public String name();



	public interface Creator<U extends SimpleSessionUser>
	{
		public U createUser(SimpleAuthenticationInformation parameter);
	}



	public class Default implements SimpleSessionUser
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String name;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final String name)
		{
			super();
			this.name = name;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String name()
		{
			return this.name;
		}

	}

}
