package net.jadoth.swizzling.types;

import net.jadoth.chars.ObjectStringConverter;
import net.jadoth.chars.VarString;
import net.jadoth.chars._charArrayRange;

public interface SwizzleIdStrategyStringConverter extends ObjectStringConverter<SwizzleIdStrategy>
{
	public static SwizzleIdStrategyStringConverter New()
	{
		return new SwizzleIdStrategyStringConverter.Implementation();
	}
	
	public final class Implementation implements SwizzleIdStrategyStringConverter
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public VarString assemble(final VarString vs, final SwizzleIdStrategy subject)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectStringAssembler<SwizzleIdStrategy>#assemble()
		}

		@Override
		public SwizzleIdStrategy parse(final _charArrayRange input)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectStringParser<SwizzleIdStrategy>#parse()
		}
		
	}
	
}
