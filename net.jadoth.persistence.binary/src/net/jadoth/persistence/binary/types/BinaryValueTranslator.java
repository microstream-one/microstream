package net.jadoth.persistence.binary.types;

import net.jadoth.low.XVM;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.reflect.XReflect;
import net.jadoth.swizzling.types.SwizzleObjectIdResolving;

public interface BinaryValueTranslator
{
	/**
	 * If target is <code>null</code>, targetOffset is interpreted as an absolute value, meaning a targetAddress.
	 * See "Unsafe" JavaDoc for background information.
	 */
	public long translateValue(long sourceAddress, Object target, long targetOffset, SwizzleObjectIdResolving idResolver);
	
	public static int to_int(final boolean value)
	{
		return value
			? 1
			: 0
		;
	}
	
	
	public static long skip_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		return sourceAddress + XVM.byteSize_byte();
	}
	
	public static long skip_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		return sourceAddress + XVM.byteSize_boolean();
	}
	
	public static long skip_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		return sourceAddress + XVM.byteSize_short();
	}
	
	public static long skip_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		return sourceAddress + XVM.byteSize_char();
	}
	
	public static long skip_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		return sourceAddress + XVM.byteSize_int();
	}
	
	public static long skip_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		return sourceAddress + XVM.byteSize_float();
	}
	
	public static long skip_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		return sourceAddress + XVM.byteSize_long();
	}
	
	public static long skip_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		return sourceAddress + XVM.byteSize_double();
	}
	
	
	
	
	public static long copy_byteTo_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_byte(target, targetOffset, XVM.get_byte(sourceAddress));
		return sourceAddress + XVM.byteSize_byte();
	}

	public static long copy_byteTo_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_boolean(target, targetOffset, 0 != XVM.get_byte(sourceAddress));
		return sourceAddress + XVM.byteSize_byte();
	}

	public static long copy_byteTo_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_short(target, targetOffset, XVM.get_byte(sourceAddress));
		return sourceAddress + XVM.byteSize_byte();
	}

	public static long copy_byteTo_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_char(target, targetOffset, (char)XVM.get_byte(sourceAddress));
		return sourceAddress + XVM.byteSize_byte();
	}

	public static long copy_byteTo_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_int(target, targetOffset, XVM.get_byte(sourceAddress));
		return sourceAddress + XVM.byteSize_byte();
	}

	public static long copy_byteTo_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_float(target, targetOffset, XVM.get_byte(sourceAddress));
		return sourceAddress + XVM.byteSize_byte();
	}

	public static long copy_byteTo_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_long(target, targetOffset, XVM.get_byte(sourceAddress));
		return sourceAddress + XVM.byteSize_byte();
	}

	public static long copy_byteTo_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_double(target, targetOffset, XVM.get_byte(sourceAddress));
		return sourceAddress + XVM.byteSize_byte();
	}



	public static long copy_booleanTo_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_byte(target, targetOffset, (byte)to_int(XVM.get_boolean(sourceAddress)));
		return sourceAddress + XVM.byteSize_boolean();
	}

	public static long copy_booleanTo_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_boolean(target, targetOffset, XVM.get_boolean(sourceAddress));
		return sourceAddress + XVM.byteSize_boolean();
	}

	public static long copy_booleanTo_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_short(target, targetOffset, (short)to_int(XVM.get_boolean(sourceAddress)));
		return sourceAddress + XVM.byteSize_boolean();
	}

	public static long copy_booleanTo_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_char(target, targetOffset, (char)to_int(XVM.get_boolean(sourceAddress)));
		return sourceAddress + XVM.byteSize_boolean();
	}

	public static long copy_booleanTo_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_int(target, targetOffset, to_int(XVM.get_boolean(sourceAddress)));
		return sourceAddress + XVM.byteSize_boolean();
	}

	public static long copy_booleanTo_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_float(target, targetOffset, to_int(XVM.get_boolean(sourceAddress)));
		return sourceAddress + XVM.byteSize_boolean();
	}

	public static long copy_booleanTo_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_long(target, targetOffset, to_int(XVM.get_boolean(sourceAddress)));
		return sourceAddress + XVM.byteSize_boolean();
	}

	public static long copy_booleanTo_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_double(target, targetOffset, to_int(XVM.get_boolean(sourceAddress)));
		return sourceAddress + XVM.byteSize_boolean();
	}



	public static long copy_shortTo_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_byte(target, targetOffset, (byte)XVM.get_short(sourceAddress));
		return sourceAddress + XVM.byteSize_short();
	}

	public static long copy_shortTo_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_boolean(target, targetOffset, 0 != XVM.get_short(sourceAddress));
		return sourceAddress + XVM.byteSize_short();
	}

	public static long copy_shortTo_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_short(target, targetOffset, XVM.get_short(sourceAddress));
		return sourceAddress + XVM.byteSize_short();
	}

	public static long copy_shortTo_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_char(target, targetOffset, (char)XVM.get_short(sourceAddress));
		return sourceAddress + XVM.byteSize_short();
	}

	public static long copy_shortTo_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_int(target, targetOffset, XVM.get_short(sourceAddress));
		return sourceAddress + XVM.byteSize_short();
	}

	public static long copy_shortTo_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_float(target, targetOffset, XVM.get_short(sourceAddress));
		return sourceAddress + XVM.byteSize_short();
	}

	public static long copy_shortTo_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_long(target, targetOffset, XVM.get_short(sourceAddress));
		return sourceAddress + XVM.byteSize_short();
	}

	public static long copy_shortTo_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_double(target, targetOffset, XVM.get_short(sourceAddress));
		return sourceAddress + XVM.byteSize_short();
	}



	public static long copy_charTo_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_byte(target, targetOffset, (byte)XVM.get_char(sourceAddress));
		return sourceAddress + XVM.byteSize_char();
	}

	public static long copy_charTo_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_boolean(target, targetOffset, 0 != XVM.get_char(sourceAddress));
		return sourceAddress + XVM.byteSize_char();
	}

	public static long copy_charTo_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_short(target, targetOffset, (short)XVM.get_char(sourceAddress));
		return sourceAddress + XVM.byteSize_char();
	}

	public static long copy_charTo_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_char(target, targetOffset, XVM.get_char(sourceAddress));
		return sourceAddress + XVM.byteSize_char();
	}

	public static long copy_charTo_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_int(target, targetOffset, XVM.get_char(sourceAddress));
		return sourceAddress + XVM.byteSize_char();
	}

	public static long copy_charTo_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_float(target, targetOffset, XVM.get_char(sourceAddress));
		return sourceAddress + XVM.byteSize_char();
	}

	public static long copy_charTo_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_long(target, targetOffset, XVM.get_char(sourceAddress));
		return sourceAddress + XVM.byteSize_char();
	}

	public static long copy_charTo_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_double(target, targetOffset, XVM.get_char(sourceAddress));
		return sourceAddress + XVM.byteSize_char();
	}



	public static long copy_intTo_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_byte(target, targetOffset, (byte)XVM.get_int(sourceAddress));
		return sourceAddress + XVM.byteSize_int();
	}

	public static long copy_intTo_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_boolean(target, targetOffset, 0 != XVM.get_int(sourceAddress));
		return sourceAddress + XVM.byteSize_int();
	}

	public static long copy_intTo_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_short(target, targetOffset, (short)XVM.get_int(sourceAddress));
		return sourceAddress + XVM.byteSize_int();
	}

	public static long copy_intTo_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_char(target, targetOffset, (char)XVM.get_int(sourceAddress));
		return sourceAddress + XVM.byteSize_int();
	}

	public static long copy_intTo_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_int(target, targetOffset, XVM.get_int(sourceAddress));
		return sourceAddress + XVM.byteSize_int();
	}
	public static long copy_intTo_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_float(target, targetOffset, XVM.get_int(sourceAddress));
		return sourceAddress + XVM.byteSize_int();
	}

	public static long copy_intTo_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_long(target, targetOffset, XVM.get_int(sourceAddress));
		return sourceAddress + XVM.byteSize_int();
	}

	public static long copy_intTo_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_double(target, targetOffset, XVM.get_int(sourceAddress));
		return sourceAddress + XVM.byteSize_int();
	}



	public static long copy_floatTo_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_byte(target, targetOffset, (byte)XVM.get_float(sourceAddress));
		return sourceAddress + XVM.byteSize_float();
	}

	public static long copy_floatTo_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_boolean(target, targetOffset, 0 != XVM.get_float(sourceAddress));
		return sourceAddress + XVM.byteSize_float();
	}

	public static long copy_floatTo_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_short(target, targetOffset, (short)XVM.get_float(sourceAddress));
		return sourceAddress + XVM.byteSize_float();
	}

	public static long copy_floatTo_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_char(target, targetOffset, (char)XVM.get_float(sourceAddress));
		return sourceAddress + XVM.byteSize_float();
	}

	public static long copy_floatTo_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_int(target, targetOffset, (int)XVM.get_float(sourceAddress));
		return sourceAddress + XVM.byteSize_float();
	}

	public static long copy_floatTo_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_float(target, targetOffset, XVM.get_float(sourceAddress));
		return sourceAddress + XVM.byteSize_float();
	}

	public static long copy_floatTo_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_long(target, targetOffset, (long)XVM.get_float(sourceAddress));
		return sourceAddress + XVM.byteSize_float();
	}

	public static long copy_floatTo_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_double(target, targetOffset, XVM.get_float(sourceAddress));
		return sourceAddress + XVM.byteSize_float();
	}



	public static long copy_longTo_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_byte(target, targetOffset, (byte)XVM.get_long(sourceAddress));
		return sourceAddress + XVM.byteSize_long();
	}

	public static long copy_longTo_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_boolean(target, targetOffset, 0 != XVM.get_long(sourceAddress));
		return sourceAddress + XVM.byteSize_long();
	}

	public static long copy_longTo_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_short(target, targetOffset, (short)XVM.get_long(sourceAddress));
		return sourceAddress + XVM.byteSize_long();
	}

	public static long copy_longTo_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_char(target, targetOffset, (char)XVM.get_long(sourceAddress));
		return sourceAddress + XVM.byteSize_long();
	}

	public static long copy_longTo_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_int(target, targetOffset, (int)XVM.get_long(sourceAddress));
		return sourceAddress + XVM.byteSize_long();
	}

	public static long copy_longTo_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_float(target, targetOffset, XVM.get_long(sourceAddress));
		return sourceAddress + XVM.byteSize_long();
	}

	public static long copy_longTo_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_long(target, targetOffset, XVM.get_long(sourceAddress));
		return sourceAddress + XVM.byteSize_long();
	}

	public static long copy_longTo_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_double(target, targetOffset, XVM.get_long(sourceAddress));
		return sourceAddress + XVM.byteSize_long();
	}



	public static long copy_doubleTo_byte(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_byte(target, targetOffset, (byte)XVM.get_double(sourceAddress));
		return sourceAddress + XVM.byteSize_double();
	}

	public static long copy_doubleTo_boolean(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_boolean(target, targetOffset, 0 != XVM.get_double(sourceAddress));
		return sourceAddress + XVM.byteSize_double();
	}

	public static long copy_doubleTo_short(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_short(target, targetOffset, (short)XVM.get_double(sourceAddress));
		return sourceAddress + XVM.byteSize_double();
	}

	public static long copy_doubleTo_char(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_char(target, targetOffset, (char)XVM.get_double(sourceAddress));
		return sourceAddress + XVM.byteSize_double();
	}

	public static long copy_doubleTo_int(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_int(target, targetOffset, (int)XVM.get_double(sourceAddress));
		return sourceAddress + XVM.byteSize_double();
	}

	public static long copy_doubleTo_float(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_float(target, targetOffset, (float)XVM.get_double(sourceAddress));
		return sourceAddress + XVM.byteSize_double();
	}

	public static long copy_doubleTo_long(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_long(target, targetOffset, (long)XVM.get_double(sourceAddress));
		return sourceAddress + XVM.byteSize_double();
	}

	public static long copy_doubleTo_double(final long sourceAddress, final Object target, final long targetOffset, final SwizzleObjectIdResolving idResolver)
	{
		XVM.set_double(target, targetOffset, XVM.get_double(sourceAddress));
		return sourceAddress + XVM.byteSize_double();
	}

	
	
	public static BinaryValueTranslator.Creator Creator()
	{
		return new BinaryValueTranslator.Creator.Implementation();
	}
		
	@FunctionalInterface
	public interface Creator
	{
		public BinaryValueTranslator createValueTranslator(
			PersistenceTypeDescriptionMember sourceMember,
			PersistenceTypeDescriptionMember targetMember
		);
		
		public final class Implementation implements BinaryValueTranslator.Creator
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
			public final BinaryValueTranslator createValueTranslator(
				final PersistenceTypeDescriptionMember sourceMember,
				final PersistenceTypeDescriptionMember targetMember
			)
			{
				if(sourceMember.isReference())
				{
					return createReferenceValueTranslator(sourceMember, targetMember);
				}

				// identical types use direct copying
				return createPrimitiveValueTranslator(sourceMember, targetMember);
			}
			
			private static BinaryValueTranslator createReferenceValueTranslator(
				final PersistenceTypeDescriptionMember sourceMember,
				final PersistenceTypeDescriptionMember targetMember
			)
			{
				// all references are stored as OID primitive values (long)
				if(targetMember == null)
				{
					return BinaryValueTranslator::skip_long;
				}
				
				if(!targetMember.isReference())
				{
					throwUnhandledTypeCompatibilityException(sourceMember.typeName(), targetMember.typeName());
				}
				
				return BinaryValueTranslator::copy_longTo_long;
			}
			
			private static BinaryValueTranslator createPrimitiveValueTranslator(
				final PersistenceTypeDescriptionMember sourceMember,
				final PersistenceTypeDescriptionMember targetMember
			)
			{
				final Class<?> sourcePrimitiveType = XReflect.primitiveType(sourceMember.typeName());
				final Class<?> targetPrimitiveType = targetMember != null
					? XReflect.primitiveType(targetMember.typeName())
					: null
				;
				
				if(sourcePrimitiveType == byte.class)
				{
					return createTranslator_byte(targetPrimitiveType);
				}
				else if(sourcePrimitiveType == boolean.class)
				{
					return createTranslator_boolean(targetPrimitiveType);
				}
				else if(sourcePrimitiveType == short.class)
				{
					return createTranslator_short(targetPrimitiveType);
				}
				else if(sourcePrimitiveType == char.class)
				{
					return createTranslator_char(targetPrimitiveType);
				}
				else if(sourcePrimitiveType == int.class)
				{
					return createTranslator_int(targetPrimitiveType);
				}
				else if(sourcePrimitiveType == float.class)
				{
					return createTranslator_float(targetPrimitiveType);
				}
				else if(sourcePrimitiveType == long.class)
				{
					return createTranslator_long(targetPrimitiveType);
				}
				else if(sourcePrimitiveType == double.class)
				{
					return createTranslator_double(targetPrimitiveType);
				}
				else
				{
					return throwUnhandledPrimitiveTypeException(targetPrimitiveType);
				}
			}
			
			private static BinaryValueTranslator createTranslator_byte(final Class<?> targetType)
			{
				return targetType == null
					? BinaryValueTranslator::skip_byte
					: targetType == byte.class
					? BinaryValueTranslator::copy_byteTo_byte
					: targetType == boolean.class
					? BinaryValueTranslator::copy_byteTo_boolean
					: targetType == short.class
					? BinaryValueTranslator::copy_byteTo_short
					: targetType == char.class
					? BinaryValueTranslator::copy_byteTo_char
					: targetType == int.class
					? BinaryValueTranslator::copy_byteTo_int
					: targetType == float.class
					? BinaryValueTranslator::copy_byteTo_float
					: targetType == long.class
					? BinaryValueTranslator::copy_byteTo_long
					: targetType == double.class
					? BinaryValueTranslator::copy_byteTo_double
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createTranslator_boolean(final Class<?> targetType)
			{
				return targetType == null
					? BinaryValueTranslator::skip_boolean
					: targetType == byte.class
					? BinaryValueTranslator::copy_booleanTo_byte
					: targetType == boolean.class
					? BinaryValueTranslator::copy_booleanTo_boolean
					: targetType == short.class
					? BinaryValueTranslator::copy_booleanTo_short
					: targetType == char.class
					? BinaryValueTranslator::copy_booleanTo_char
					: targetType == int.class
					? BinaryValueTranslator::copy_booleanTo_int
					: targetType == float.class
					? BinaryValueTranslator::copy_booleanTo_float
					: targetType == long.class
					? BinaryValueTranslator::copy_booleanTo_long
					: targetType == double.class
					? BinaryValueTranslator::copy_booleanTo_double
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createTranslator_short(final Class<?> targetType)
			{
				return targetType == null
					? BinaryValueTranslator::skip_short
					: targetType == byte.class
					? BinaryValueTranslator::copy_shortTo_byte
					: targetType == boolean.class
					? BinaryValueTranslator::copy_shortTo_boolean
					: targetType == short.class
					? BinaryValueTranslator::copy_shortTo_short
					: targetType == char.class
					? BinaryValueTranslator::copy_shortTo_char
					: targetType == int.class
					? BinaryValueTranslator::copy_shortTo_int
					: targetType == float.class
					? BinaryValueTranslator::copy_shortTo_float
					: targetType == long.class
					? BinaryValueTranslator::copy_shortTo_long
					: targetType == double.class
					? BinaryValueTranslator::copy_shortTo_double
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createTranslator_char(final Class<?> targetType)
			{
				return targetType == null
					? BinaryValueTranslator::skip_char
					: targetType == byte.class
					? BinaryValueTranslator::copy_charTo_byte
					: targetType == boolean.class
					? BinaryValueTranslator::copy_charTo_boolean
					: targetType == short.class
					? BinaryValueTranslator::copy_charTo_short
					: targetType == char.class
					? BinaryValueTranslator::copy_charTo_char
					: targetType == int.class
					? BinaryValueTranslator::copy_charTo_int
					: targetType == float.class
					? BinaryValueTranslator::copy_charTo_float
					: targetType == long.class
					? BinaryValueTranslator::copy_charTo_long
					: targetType == double.class
					? BinaryValueTranslator::copy_charTo_double
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createTranslator_int(final Class<?> targetType)
			{
				return targetType == null
					? BinaryValueTranslator::skip_int
					: targetType == byte.class
					? BinaryValueTranslator::copy_intTo_byte
					: targetType == boolean.class
					? BinaryValueTranslator::copy_intTo_boolean
					: targetType == short.class
					? BinaryValueTranslator::copy_intTo_short
					: targetType == char.class
					? BinaryValueTranslator::copy_intTo_char
					: targetType == int.class
					? BinaryValueTranslator::copy_intTo_int
					: targetType == float.class
					? BinaryValueTranslator::copy_intTo_float
					: targetType == long.class
					? BinaryValueTranslator::copy_intTo_long
					: targetType == double.class
					? BinaryValueTranslator::copy_intTo_double
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createTranslator_float(final Class<?> targetType)
			{
				return targetType == null
					? BinaryValueTranslator::skip_float
					: targetType == byte.class
					? BinaryValueTranslator::copy_floatTo_byte
					: targetType == boolean.class
					? BinaryValueTranslator::copy_floatTo_boolean
					: targetType == short.class
					? BinaryValueTranslator::copy_floatTo_short
					: targetType == char.class
					? BinaryValueTranslator::copy_floatTo_char
					: targetType == int.class
					? BinaryValueTranslator::copy_floatTo_int
					: targetType == float.class
					? BinaryValueTranslator::copy_floatTo_float
					: targetType == long.class
					? BinaryValueTranslator::copy_floatTo_long
					: targetType == double.class
					? BinaryValueTranslator::copy_floatTo_double
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createTranslator_long(final Class<?> targetType)
			{
				return targetType == null
					? BinaryValueTranslator::skip_long
					: targetType == byte.class
					? BinaryValueTranslator::copy_longTo_byte
					: targetType == boolean.class
					? BinaryValueTranslator::copy_longTo_boolean
					: targetType == short.class
					? BinaryValueTranslator::copy_longTo_short
					: targetType == char.class
					? BinaryValueTranslator::copy_longTo_char
					: targetType == int.class
					? BinaryValueTranslator::copy_longTo_int
					: targetType == float.class
					? BinaryValueTranslator::copy_longTo_float
					: targetType == long.class
					? BinaryValueTranslator::copy_longTo_long
					: targetType == double.class
					? BinaryValueTranslator::copy_longTo_double
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createTranslator_double(final Class<?> targetType)
			{
				return targetType == null
					? BinaryValueTranslator::skip_double
					: targetType == byte.class
					? BinaryValueTranslator::copy_doubleTo_byte
					: targetType == boolean.class
					? BinaryValueTranslator::copy_doubleTo_boolean
					: targetType == short.class
					? BinaryValueTranslator::copy_doubleTo_short
					: targetType == char.class
					? BinaryValueTranslator::copy_doubleTo_char
					: targetType == int.class
					? BinaryValueTranslator::copy_doubleTo_int
					: targetType == float.class
					? BinaryValueTranslator::copy_doubleTo_float
					: targetType == long.class
					? BinaryValueTranslator::copy_doubleTo_long
					: targetType == double.class
					? BinaryValueTranslator::copy_doubleTo_double
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
				
			
			private static BinaryValueTranslator throwUnhandledPrimitiveTypeException(final Class<?> primitiveType)
			{
				// (19.09.2018 TM)EXCP: proper exception
				throw new RuntimeException("Unhandled primitive type: " + primitiveType);
			}
			
			private static void throwUnhandledTypeCompatibilityException(
				final String sourceType,
				final String targetType
			)
			{
				// (18.09.2018 TM)EXCP: proper exception
				throw new RuntimeException(
					"Cannot convert between primitive and reference values: "
					+ sourceType + " <-> " + targetType+ "."
				);
			}
			
		}
	}
	

	
}
