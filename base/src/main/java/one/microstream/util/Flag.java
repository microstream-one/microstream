package one.microstream.util;


/**
 * A wrapper for a mutable boolean type and convenience setting and getting methods.
 * 
 * 
 */
public interface Flag
{
	public boolean set(boolean state);
	
	public boolean on();

	public boolean off();
	
	public boolean isOn();
	
	public boolean isOff();

	public boolean toggle();

	
	
	public static Flag New()
	{
		return New(false);
	}

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
		public final boolean set(final boolean state)
		{
			if(state)
			{
				this.on();
				return false;
			}
			
			this.off();
			return true;
		}

		@Override
		public final boolean isOn()
		{
			return this.state;
		}

		@Override
		public final boolean isOff()
		{
			return !this.state;
		}

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
			return !(this.state = !this.state); // extremely funny syntax
		}

	}

}
