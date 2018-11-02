package net.jadoth.network.persistence;

import net.jadoth.chars.ObjectStringConverter;
import net.jadoth.chars.VarString;
import net.jadoth.chars._charArrayRange;
import net.jadoth.persistence.types.PersistenceTypeDictionaryAssembler;
import net.jadoth.swizzling.types.SwizzleIdStrategyStringConverter;


/**
 * A "StringConverter" is hereby defined as a logic instance that handles both conversion to and from a String-form.
 * 
 * @author TM
 *
 */
public interface ComProtocolStringConverter extends ObjectStringConverter<ComProtocol>
{
	@Override
	default VarString provideAssemblyBuffer()
	{
		// including the whole type dictionary makes the string rather large.
		return VarString.New(10_000);
	}
	

	
	public static String defaultLabelVersion()
	{
		return "Version";
	}
	
	public static String defaultLabelByteOrder()
	{
		return "ByteOrder";
	}
	
	public static String defaultLabelIdStrategy()
	{
		return "IdStrategy";
	}
	
	public static SwizzleIdStrategyStringConverter defaultIdStrategyStringConverter()
	{
		// light-weight and easy collectable one-shot instance instead of permanent constant instance.
		return SwizzleIdStrategyStringConverter.New();
	}
	
	public static PersistenceTypeDictionaryAssembler defaultTypeDictionaryAssembler()
	{
		// light-weight and easy collectable one-shot instance instead of permanent constant instance.
		return PersistenceTypeDictionaryAssembler.New();
	}
	
	public static String defaultLabelTypeDictionary()
	{
		return "TypeDictionary";
	}
	
	public static char defaultProtocolItemSeparator()
	{
		return ';';
	}
	
	public static char defaultProtocolItemAssigner()
	{
		return ':';
	}
	
	
	public default String labelProtocolVersion()
	{
		return defaultLabelVersion();
	}
	
	public default String labelByteOrder()
	{
		return defaultLabelByteOrder();
	}
	
	public default String labelIdStrategy()
	{
		return defaultLabelIdStrategy();
	}
	
	public default SwizzleIdStrategyStringConverter idStrategyStringConverter()
	{
		return defaultIdStrategyStringConverter();
	}
	
	public default String labelTypeDictionary()
	{
		return defaultLabelTypeDictionary();
	}
	
	public default PersistenceTypeDictionaryAssembler typeDictionaryAssembler()
	{
		return defaultTypeDictionaryAssembler();
	}
	
	public default char protocolItemSeparator()
	{
		return defaultProtocolItemSeparator();
	}
	
	public default char protocolItemAssigner()
	{
		return defaultProtocolItemAssigner();
	}
	
	
	
	public static ComProtocolStringConverter New()
	{
		return new ComProtocolStringConverter.Implementation();
	}
	
	public final class Implementation implements ComProtocolStringConverter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public VarString assemble(final VarString vs, final ComProtocol protocol)
		{
			final char sep = this.protocolItemSeparator();
			
			this.assembleName          (vs, protocol).add(sep).lf();
			this.assembleVersion       (vs, protocol).add(sep).lf();
			this.assembleByteOrder     (vs, protocol).add(sep).lf();
			this.assembleIdStrategy    (vs, protocol).add(sep).lf();
			this.assembleTypeDictionary(vs, protocol);
			
			return vs;
		}
		
		// just a short-cut method for the lengthy proper one. And yes, ass, very funny.
		private char ass()
		{
			return this.protocolItemAssigner();
		}
		
		private VarString assembleName(final VarString vs, final ComProtocol p)
		{
			return vs.add(p.name());
		}
		
		private VarString assembleVersion(final VarString vs, final ComProtocol p)
		{
			return vs.add(this.labelProtocolVersion()).add(this.ass()).blank().add(p.version());
		}
		
		private VarString assembleByteOrder(final VarString vs, final ComProtocol p)
		{
			return vs.add(this.labelByteOrder()).add(this.ass()).blank().add(p.byteOrder());
		}
		
		private VarString assembleIdStrategy(final VarString vs, final ComProtocol p)
		{
			final SwizzleIdStrategyStringConverter idsc = this.idStrategyStringConverter();
			
			return vs.add(this.labelIdStrategy()).add(this.ass()).blank().apply(v ->
				idsc.assemble(v, p.idStrategy())
			);
		}
		
		private VarString assembleTypeDictionary(final VarString vs, final ComProtocol protocol)
		{
			final PersistenceTypeDictionaryAssembler ptda = this.typeDictionaryAssembler();
			
			return vs.add(this.labelTypeDictionary()).add(this.ass()).lf().apply(v ->
				ptda.assemble(v, protocol.typeDictionary())
			) // no separator at the end, the type dictionary string is intentionally trailing
			;
		}
		
		
		
		@Override
		public ComProtocol parse(final _charArrayRange input)
		{
			// FIXME ComProtocolStringConverter.Implementation#parseProtocol()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
}
