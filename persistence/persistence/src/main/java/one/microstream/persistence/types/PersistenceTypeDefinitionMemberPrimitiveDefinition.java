package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.math.XMath;
import one.microstream.persistence.exceptions.PersistenceException;



public interface PersistenceTypeDefinitionMemberPrimitiveDefinition
extends PersistenceTypeDescriptionMemberPrimitiveDefinition, PersistenceTypeDefinitionMember
{
	@Override
	public default String identifier()
	{
		return this.primitiveDefinition();
	}
	
	
	public static PersistenceTypeDefinitionMemberPrimitiveDefinition New(
		final PersistenceTypeDescriptionMemberPrimitiveDefinition description
	)
	{
		final long persistentLength = XMath.equal(
			description.persistentMinimumLength(),
			description.persistentMaximumLength()
		);
		
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Default(
			description.primitiveDefinition(),
			persistentLength
		);
	}
	
	public static PersistenceTypeDefinitionMemberPrimitiveDefinition New(
		final Class<?> primitiveType   ,
		final long     persistentLength
	)
	{
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Default(
			Default.assemblePrimitiveDefinition(primitiveType),
			persistentLength
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberPrimitiveDefinition.Default
	implements PersistenceTypeDefinitionMemberPrimitiveDefinition
	{
		// CHECKSTYLE.OFF: ConstantName: literals and type names are intentionally unchanged
		
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		private static final String
			_bit             = " bit"            ,
			_integer_signed  = " integer signed" ,
			_integer_unicode = " integer unicode",
			_decimal_IEEE754 = " decimal IEEE754",
			_boolean         = " boolean"
		;

		private static final char[]
			DEFINITION_byte    = (Byte     .SIZE + _bit + _integer_signed ).toCharArray(),
			DEFINITION_boolean = (Byte     .SIZE + _bit + _boolean        ).toCharArray(),
			DEFINITION_short   = (Short    .SIZE + _bit + _integer_signed ).toCharArray(),
			DEFINITION_char    = (Character.SIZE + _bit + _integer_unicode).toCharArray(),
			DEFINITION_int     = (Integer  .SIZE + _bit + _integer_signed ).toCharArray(),
			DEFINITION_float   = (Float    .SIZE + _bit + _decimal_IEEE754).toCharArray(),
			DEFINITION_long    = (Long     .SIZE + _bit + _integer_signed ).toCharArray(),
			DEFINITION_double  = (Double   .SIZE + _bit + _decimal_IEEE754).toCharArray(),
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
			throw new PersistenceException("Unknown primitive definition: " + trimmed);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String primitiveDefinition,
			final long   persistentLength
		)
		{
			super(primitiveDefinition, persistentLength);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final Class<?> type()
		{
			// a definition does not have a type of a member field. The defined primitive type is in the owner type.
			return null;
		}
		
	}

}
