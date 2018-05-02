package net.jadoth.util;

public interface Flag
{
	public boolean on();

	public boolean off();

	public boolean toggle();

	public Flag set(boolean state);

	public boolean isSet();

	

	public static Flag New(final boolean state)
	{
		return new Flag.Simple(state);
	}

	public final class Simple implements Flag
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private boolean state;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Simple(final boolean state)
		{
			super();
			this.state = state;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean on()
		{
			final boolean current = this.state;
			this.state = true;
			return current;
		}

		@Override
		public final boolean off()
		{
			final boolean current = this.state;
			this.state = false;
			return current;
		}

		@Override
		public final boolean toggle()
		{
			return !(this.state = !this.state); // extremely lol syntax
		}

		@Override
		public final Simple set(final boolean state)
		{
			if(state)
			{
				this.on();
			}
			else
			{
				this.off();
			}
			return this;
		}

		@Override
		public final boolean isSet()
		{
			return this.state;
		}

	}

}

