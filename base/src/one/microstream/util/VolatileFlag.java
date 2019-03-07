package one.microstream.util;

import one.microstream.memory.XMemory;
import one.microstream.reflect.XReflect;

public final class VolatileFlag
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	// CHECKSTYLE.OFF: ConstantName: field names are intentionally unchanged

	private static final long FIELD_OFFSET_state = XMemory.objectFieldOffset(
		XReflect.getInstanceFieldOfType(VolatileFlag.class, int.class)
	);

	// CHECKSTYLE.ON: ConstantName


	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final VolatileFlag New()
	{
		return new VolatileFlag();
	}

	public static final VolatileFlag New(final boolean state)
	{
		return new VolatileFlag().set(state);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// note that this causes no memory overhead compared to a boolean as all instances get memory-aligned anyway
	private volatile int state;



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final boolean on()
	{
		return !XMemory.compareAndSwap_int(this, FIELD_OFFSET_state, 0, 1);
	}

	public final boolean off()
	{
		return XMemory.compareAndSwap_int(this, FIELD_OFFSET_state, 1, 0);
	}

	public final VolatileFlag set(final boolean state)
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

	public final boolean state()
	{
		return this.state != 0;
	}

}
