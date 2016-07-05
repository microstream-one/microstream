package net.jadoth.util;

import net.jadoth.memory.Memory;
import net.jadoth.reflect.JadothReflect;


public interface Flag
{
	public boolean on();

	public boolean off();

	public boolean toggle();

	public Flag set(boolean state);

	public boolean isSet();




	public final class Simple implements Flag
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final Simple New()
		{
			return new Simple();
		}

		public static final Simple New(final boolean state)
		{
			return new Simple().set(state);
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private boolean state;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

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

	public final class Volatile implements Flag
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		// CHECKSTYLE.OFF: ConstantName: field names are intentionally unchanged

		private static final long FIELD_OFFSET_state = Memory.objectFieldOffset(
			JadothReflect.getInstanceFieldOfType(Simple.class, boolean.class)
		);

		// CHECKSTYLE.ON: ConstantName


		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final Simple New()
		{
			return new Simple();
		}

		public static final Simple New(final boolean state)
		{
			return new Simple().set(state);
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// note that this causes no memory overhead compared to a boolean as all instances get memory-aligned anyway
		private volatile int state;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public final boolean on()
		{
			return !Memory.compareAndSwap_int(this, FIELD_OFFSET_state, 0, 1);
		}

		@Override
		public final boolean off()
		{
			return Memory.compareAndSwap_int(this, FIELD_OFFSET_state, 1, 0);
		}

		@Override
		public final boolean toggle()
		{
			return this.state == 0  ? this.on()  : this.off();
		}

		@Override
		public final Volatile set(final boolean state)
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
			return this.state != 0;
		}

	}

}

