package net.jadoth.swizzling.types;

import net.jadoth.chars.ObjectStringConverter;
import net.jadoth.chars.VarString;
import net.jadoth.chars._charArrayRange;

public interface SwizzleIdStrategyStringConverter extends ObjectStringConverter<SwizzleIdStrategy>
{
	@Override
	public VarString assemble(VarString vs, SwizzleIdStrategy subject);
	
	@Override
	public default VarString provideAssemblyBuffer()
	{
		return ObjectStringConverter.super.provideAssemblyBuffer();
	}
	
	@Override
	public default String assemble(final SwizzleIdStrategy subject)
	{
		return ObjectStringConverter.super.assemble(subject);
	}
	
	@Override
	public SwizzleIdStrategy parse(_charArrayRange input);

	@Override
	public default SwizzleIdStrategy parse(final String input)
	{
		return ObjectStringConverter.super.parse(input);
	}
	
	
	public static SwizzleIdStrategyStringConverter New()
	{
		return new SwizzleIdStrategyStringConverter.Implementation();
	}
	
	public final class Implementation implements SwizzleIdStrategyStringConverter
	{

		@Override
		public VarString assemble(final VarString vs, final SwizzleIdStrategy subject)
		{
			// FIXME SwizzleIdStrategyStringConverter#assemble()
			throw new net.jadoth.meta.NotImplementedYetError();
		}

		@Override
		public SwizzleIdStrategy parse(final _charArrayRange input)
		{
			// FIXME SwizzleIdStrategyStringConverter#parse()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
		
}
