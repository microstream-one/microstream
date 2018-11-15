package net.jadoth.com;

import java.nio.ByteOrder;

import net.jadoth.chars.VarString;
import net.jadoth.chars.XChars;
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
	
	
	public static int defaultLengthDigitCount()
	{
		return 8;
	}
	
	public static byte[] assembleSendableProtocolBytes(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		return assembleSendableProtocolBytes(protocol, protocolStringConverter, defaultLengthDigitCount());
	}
	
	public static byte[] assembleSendableProtocolBytes(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final int                        lengthCharCount
	)
	{
		// encode uses by default UTF-8
		return assembleSendableProtocolString(protocol, protocolStringConverter, lengthCharCount).encode();
	}
	
	public static VarString assembleSendableProtocolString(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final int                        lengthCharCount
	)
	{
		final VarString vs = VarString.New(10_000);
		
		return assembleSendableProtocolString(vs, protocol, protocolStringConverter, lengthCharCount);
	}
	
	public static VarString assembleSendableProtocolString(
		final VarString                  vs                     ,
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final int                        lengthCharCount
	)
	{
		vs
		.reset()
		.repeat(lengthCharCount, '0')
		.add(protocolStringConverter.protocolItemSeparator())
		;
		protocolStringConverter.assemble(vs, protocol);
		
		final char[] lengthString = XChars.toString(vs.length()).toCharArray();
		vs.setChars(lengthCharCount - lengthString.length, lengthString);
		
		return vs;
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
