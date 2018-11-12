package net.jadoth.com;

import java.nio.ByteOrder;

import net.jadoth.low.XVM;

public class Com
{
	public static long defaultObjectIdBaseServer()
	{
		return 9_200_000_000_000_000_000L;
	}
	
	public static long defaultObjectIdBaseClient()
	{
		return 9_100_000_000_000_000_000L;
	}
	
	public static ComFoundation.Default<?> Foundation()
	{
		return ComFoundation.New();
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategy(final long startingObjectId)
	{
		return ComDefaultIdStrategy.New(startingObjectId);
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategyServer()
	{
		return DefaultIdStrategy(defaultObjectIdBaseServer());
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategyClient()
	{
		return DefaultIdStrategy(defaultObjectIdBaseClient());
	}
	
	public static ByteOrder byteOrder()
	{
		return XVM.nativeByteOrder();
	}
			
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private Com()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
