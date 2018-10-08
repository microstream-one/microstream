package net.jadoth.persistence.types;

import net.jadoth.chars.VarString;
import net.jadoth.chars.XChars;
import net.jadoth.low.XVM;



public interface PersistenceTypeDefinitionMemberPrimitiveDefinition
extends PersistenceTypeDescriptionMemberPrimitiveDefinition, PersistenceTypeDefinitionMember
{
	public static PersistenceTypeDefinitionMemberPrimitiveDefinition New(
		final PersistenceTypeDescriptionMemberPrimitiveDefinition description
	)
	{
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Implementation(
			description.name()                   ,
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
	}
	
	public static PersistenceTypeDefinitionMemberPrimitiveDefinition New(
		final Class<?> primitiveType          ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Implementation(
			Implementation.assemblePrimitiveDefinition(primitiveType),
			persistentMinimumLength,
			persistentMaximumLength
		);
	}

	public class Implementation
	extends PersistenceTypeDescriptionMemberPrimitiveDefinition.Implementation
	implements
	PersistenceTypeDefinitionMemberPrimitiveDefinition
	{
		// CHECKSTYLE.OFF: ConstantName: literals and type names are intentionally unchanged
		
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final String
			_bit             = " bit"            ,
			_integer_signed  = " integer signed" ,
			_integer_unicode = " integer unicode",
			_decimal_IEEE754 = " decimal IEEE754",
			_boolean         = " boolean"
		;

		private static final char[]
			DEFINITION_byte    = (XVM.bitSize_byte()    + _bit + _integer_signed ).toCharArray(),
			DEFINITION_boolean = (XVM.bitSize_boolean() + _bit + _boolean        ).toCharArray(),
			DEFINITION_short   = (XVM.bitSize_short()   + _bit + _integer_signed ).toCharArray(),
			DEFINITION_char    = (XVM.bitSize_char()    + _bit + _integer_unicode).toCharArray(),
			DEFINITION_int     = (XVM.bitSize_int()     + _bit + _integer_signed ).toCharArray(),
			DEFINITION_float   = (XVM.bitSize_float()   + _bit + _decimal_IEEE754).toCharArray(),
			DEFINITION_long    = (XVM.bitSize_long()    + _bit + _integer_signed ).toCharArray(),
			DEFINITION_double  = (XVM.bitSize_double()  + _bit + _decimal_IEEE754).toCharArray(),
			DEFINITION_void    = void.class.getSimpleName().toCharArray()
		;

		// CHECKSTYLE.ON: ConstantName
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final VarString assemblePrimitiveDefinition(final VarString vc, final Class<?> primitiveType)
		{
			if(primitiveType == byte.class)
			{
				return vc.add(DEFINITION_byte);
			}
			if(primitiveType == boolean.class)
			{
				return vc.add(DEFINITION_boolean);
			}
			if(primitiveType == short.class)
			{
				return vc.add(DEFINITION_short);
			}
			if(primitiveType == char.class)
			{
				return vc.add(DEFINITION_char);
			}
			if(primitiveType == int.class)
			{
				return vc.add(DEFINITION_int);
			}
			if(primitiveType == float.class)
			{
				return vc.add(DEFINITION_float);
			}
			if(primitiveType == long.class)
			{
				return vc.add(DEFINITION_long);
			}
			if(primitiveType == double.class)
			{
				return vc.add(DEFINITION_double);
			}
			if(primitiveType == void.class)
			{
				return vc.add(DEFINITION_void);
			}
			throw new IllegalArgumentException();
		}
		
		public static final String assemblePrimitiveDefinition(final Class<?> primitiveType)
		{
			return assemblePrimitiveDefinition(VarString.New(), primitiveType).toString();
		}

		public static final Class<?> resolvePrimitiveDefinition(final String primitiveDefinition)
		{
			// trim string just in case, will be very fast / won't create a new instance if unnecessary
			final String trimmed = primitiveDefinition.trim();

			if(XChars.equals(trimmed, DEFINITION_byte, 0))
			{
				return byte.class;
			}
			if(XChars.equals(trimmed, DEFINITION_boolean, 0))
			{
				return boolean.class;
			}
			if(XChars.equals(trimmed, DEFINITION_short, 0))
			{
				return short.class;
			}
			if(XChars.equals(trimmed, DEFINITION_char, 0))
			{
				return char.class;
			}
			if(XChars.equals(trimmed, DEFINITION_int, 0))
			{
				return int.class;
			}
			if(XChars.equals(trimmed, DEFINITION_float, 0))
			{
				return float.class;
			}
			if(XChars.equals(trimmed, DEFINITION_long, 0))
			{
				return long.class;
			}
			if(XChars.equals(trimmed, DEFINITION_double, 0))
			{
				return double.class;
			}
			if(XChars.equals(trimmed, DEFINITION_void, 0))
			{
				return void.class;
			}
			// (02.05.2014)EXCP: proper exception
			throw new RuntimeException("Unknown primitive definition: " + trimmed);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final String primitiveDefinition    ,
			final long   persistentMinimumLength,
			final long   persistentMaximumLength
		)
		{
			super(primitiveDefinition, persistentMinimumLength, persistentMaximumLength);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> type()
		{
			// a definition does not have a member / field type. The defined primitive type is in the owner type.
			return null;
		}
		
	}

}
