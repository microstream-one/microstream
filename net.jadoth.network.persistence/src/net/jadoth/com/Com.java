package net.jadoth.com;

import java.nio.ByteBuffer;
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
	
	// (18.11.2018 TM)FIXME: use or remove default id strategies
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
	
	public static int defaultPort()
	{
		// arbitrary, totally random port. Totally random, really.
		return 1099;
	}
	
	
	
	public static ComFoundation.Default<?> Foundation()
	{
		return ComFoundation.New();
	}
	
	
	public static int defaultProtocolLengthDigitCount()
	{
		return 8;
	}
	
	public static ByteBuffer bufferProtocol(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		return bufferProtocol(protocol, protocolStringConverter, defaultProtocolLengthDigitCount());
	}
	
	public static ByteBuffer bufferProtocol(
		final ComProtocol                protocol                ,
		final ComProtocolStringConverter protocolStringConverter ,
		final int                        protocolLengthDigitCount
	)
	{
		final byte[] assembledProtocolBytes = Com.assembleSendableProtocolBytes(
			protocol               ,
			protocolStringConverter,
			protocolLengthDigitCount
		);
		
		// the ByteBuffer#put(byte[]) is, of course, a catastrophe, as usual in JDK code. Hence the direct way.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(assembledProtocolBytes.length);
		final long dbbAddress = XVM.getDirectByteBufferAddress(dbb);
		XVM.copyArray(assembledProtocolBytes, dbbAddress);
		// the bytebuffer's position remains at 0, limit at capacity. Both are correct for the first reading call.
		
		return dbb;
	}
	
	public static byte[] assembleSendableProtocolBytes(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		return assembleSendableProtocolBytes(protocol, protocolStringConverter, defaultProtocolLengthDigitCount());
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
