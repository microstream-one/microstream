package net.jadoth.persistence.binary.types;

import net.jadoth.low.XVM;
import net.jadoth.math.XMath;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.reflect.XReflect;

public interface BinaryValueTranslator
{
	public void translateValue(long sourceAddress, long targetAddress);
	
	
	
	public static BinaryValueTranslator.Creator Creator()
	{
		return new BinaryValueTranslator.Creator.Implementation();
	}
	
	@FunctionalInterface
	public interface Creator
	{
		public BinaryValueTranslator createValueTranslator(
			PersistenceTypeDescriptionMember sourceMember      ,
			int                              sourceMemberOffset,
			PersistenceTypeDescriptionMember targetMember      ,
			int                              targetMemberOffset
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
				final PersistenceTypeDescriptionMember sourceMember      ,
				final int                              sourceMemberOffset,
				final PersistenceTypeDescriptionMember targetMember      ,
				final int                              targetMemberOffset
			)
			{
				// offset validation
				XMath.positive(sourceMemberOffset);
				XMath.positive(targetMemberOffset);
				
				if(sourceMember.isReference())
				{
					if(!targetMember.isReference())
					{
						throwUnhandledTypeCompatibilityException(sourceMember.typeName(), targetMember.typeName());
					}
					
					// all references are stored as OID primitive values
					return BinaryValueTranslator.New(
						(int)BinaryPersistence.oidLength(),
						sourceMemberOffset,
						targetMemberOffset
					);
				}

				// identical types use direct copying
				return createPrimitiveValueTranslator(sourceMember, sourceMemberOffset, targetMember, targetMemberOffset);
			}
			
			private static BinaryValueTranslator createPrimitiveValueTranslator(
				final PersistenceTypeDescriptionMember sourceMember      ,
				final int                              sourceMemberOffset,
				final PersistenceTypeDescriptionMember targetMember      ,
				final int                              targetMemberOffset
			)
			{
				final Class<?> sourcePrimitiveType = XReflect.primitiveType(sourceMember.typeName());
				final Class<?> targetPrimitiveType = XReflect.primitiveType(targetMember.typeName());
				
				if(sourcePrimitiveType == byte.class)
				{
					return createConverter_byte(sourceMemberOffset, targetPrimitiveType, targetMemberOffset);
				}
				else if(sourcePrimitiveType == boolean.class)
				{
					return createConverter_boolean(sourceMemberOffset, targetPrimitiveType, targetMemberOffset);
				}
				else if(sourcePrimitiveType == short.class)
				{
					return createConverter_short(sourceMemberOffset, targetPrimitiveType, targetMemberOffset);
				}
				else if(sourcePrimitiveType == char.class)
				{
					return createConverter_char(sourceMemberOffset, targetPrimitiveType, targetMemberOffset);
				}
				else if(sourcePrimitiveType == int.class)
				{
					return createConverter_int(sourceMemberOffset, targetPrimitiveType, targetMemberOffset);
				}
				else if(sourcePrimitiveType == float.class)
				{
					return createConverter_float(sourceMemberOffset, targetPrimitiveType, targetMemberOffset);
				}
				else if(sourcePrimitiveType == long.class)
				{
					return createConverter_long(sourceMemberOffset, targetPrimitiveType, targetMemberOffset);
				}
				else if(sourcePrimitiveType == double.class)
				{
					return createConverter_double(sourceMemberOffset, targetPrimitiveType, targetMemberOffset);
				}
				else
				{
					return throwUnhandledPrimitiveTypeException(targetPrimitiveType);
				}
			}
									
			private static int to_int(final boolean value)
			{
				return value
					? 1
					: 0
				;
			}
			
			private static BinaryValueTranslator createConverter_byte(
				final int      srcOffset ,
				final Class<?> targetType,
				final int      trgOffset
			)
			{
				// super compact and still nicely breakpoint-debuggable.
				return targetType == byte.class
					? Size1(srcOffset, trgOffset)
					: targetType == boolean.class
					? (srcAddress, trgAddress) ->
						XVM.set_boolean(trgAddress + trgOffset, 0 != XVM.get_byte(srcAddress + srcOffset))
					: targetType == short.class
					? (srcAddress, trgAddress) ->
						XVM.set_short(trgAddress + trgOffset, XVM.get_byte(srcAddress + srcOffset))
					: targetType == char.class
					? (srcAddress, trgAddress) ->
						XVM.set_char(trgAddress + trgOffset, (char)XVM.get_byte(srcAddress + srcOffset))
					: targetType == int.class
					? (srcAddress, trgAddress) ->
						XVM.set_int(trgAddress + trgOffset, XVM.get_byte(srcAddress + srcOffset))
					: targetType == float.class
					? (srcAddress, trgAddress) ->
						XVM.set_float(trgAddress + trgOffset, XVM.get_byte(srcAddress + srcOffset))
					: targetType == long.class
					? (srcAddress, trgAddress) ->
						XVM.set_long(trgAddress + trgOffset, XVM.get_byte(srcAddress + srcOffset))
					: targetType == double.class
					? (srcAddress, trgAddress) ->
						XVM.set_double(trgAddress + trgOffset, XVM.get_byte(srcAddress + srcOffset))
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createConverter_boolean(
				final int      srcOffset ,
				final Class<?> targetType,
				final int      trgOffset
			)
			{
				// super compact and still nicely breakpoint-debuggable.
				return targetType == byte.class
					? (srcAddress, trgAddress) ->
						XVM.set_byte(trgAddress + trgOffset, (byte)to_int(XVM.get_boolean(srcAddress + srcOffset)))
					: targetType == boolean.class
					? Size1(srcOffset, trgOffset)
					: targetType == short.class
					? (srcAddress, trgAddress) ->
						XVM.set_short(trgAddress + trgOffset, (short)to_int(XVM.get_boolean(srcAddress + srcOffset)))
					: targetType == char.class
					? (srcAddress, trgAddress) ->
						XVM.set_char(trgAddress + trgOffset, (char)to_int(XVM.get_boolean(srcAddress + srcOffset)))
					: targetType == int.class
					? (srcAddress, trgAddress) ->
						XVM.set_int(trgAddress + trgOffset, to_int(XVM.get_boolean(srcAddress + srcOffset)))
					: targetType == float.class
					? (srcAddress, trgAddress) ->
						XVM.set_float(trgAddress + trgOffset, to_int(XVM.get_boolean(srcAddress + srcOffset)))
					: targetType == long.class
					? (srcAddress, trgAddress) ->
						XVM.set_long(trgAddress + trgOffset, to_int(XVM.get_boolean(srcAddress + srcOffset)))
					: targetType == double.class
					? (srcAddress, trgAddress) ->
						XVM.set_double(trgAddress + trgOffset, to_int(XVM.get_boolean(srcAddress + srcOffset)))
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createConverter_short(
				final int      srcOffset ,
				final Class<?> targetType,
				final int      trgOffset
			)
			{
				// super compact and still nicely breakpoint-debuggable.
				return targetType == byte.class
					? (srcAddress, trgAddress) ->
						XVM.set_byte(trgAddress + trgOffset, (byte)XVM.get_short(srcAddress + srcOffset))
					: targetType == boolean.class
					? (srcAddress, trgAddress) ->
						XVM.set_boolean(trgAddress + trgOffset, 0 != XVM.get_short(srcAddress + srcOffset))
					: targetType == short.class
					? Size2(srcOffset, trgOffset)
					: targetType == char.class
					? (srcAddress, trgAddress) ->
						XVM.set_char(trgAddress + trgOffset, (char)XVM.get_short(srcAddress + srcOffset))
					: targetType == int.class
					? (srcAddress, trgAddress) ->
						XVM.set_int(trgAddress + trgOffset, XVM.get_short(srcAddress + srcOffset))
					: targetType == float.class
					? (srcAddress, trgAddress) ->
						XVM.set_float(trgAddress + trgOffset, XVM.get_short(srcAddress + srcOffset))
					: targetType == long.class
					? (srcAddress, trgAddress) ->
						XVM.set_long(trgAddress + trgOffset, XVM.get_short(srcAddress + srcOffset))
					: targetType == double.class
					? (srcAddress, trgAddress) ->
						XVM.set_double(trgAddress + trgOffset, XVM.get_short(srcAddress + srcOffset))
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createConverter_char(
				final int      srcOffset ,
				final Class<?> targetType,
				final int      trgOffset
			)
			{
				// super compact and still nicely breakpoint-debuggable.
				return targetType == byte.class
					? (srcAddress, trgAddress) ->
						XVM.set_byte(trgAddress + trgOffset, (byte)XVM.get_char(srcAddress + srcOffset))
					: targetType == boolean.class
					? (srcAddress, trgAddress) ->
						XVM.set_boolean(trgAddress + trgOffset, 0 != XVM.get_char(srcAddress + srcOffset))
					: targetType == short.class
					? (srcAddress, trgAddress) ->
						XVM.set_short(trgAddress + trgOffset, (short)XVM.get_char(srcAddress + srcOffset))
					: targetType == char.class
					? Size2(srcOffset, trgOffset)
					: targetType == int.class
					? (srcAddress, trgAddress) ->
						XVM.set_int(trgAddress + trgOffset, XVM.get_char(srcAddress + srcOffset))
					: targetType == float.class
					? (srcAddress, trgAddress) ->
						XVM.set_float(trgAddress + trgOffset, XVM.get_char(srcAddress + srcOffset))
					: targetType == long.class
					? (srcAddress, trgAddress) ->
						XVM.set_long(trgAddress + trgOffset, XVM.get_char(srcAddress + srcOffset))
					: targetType == double.class
					? (srcAddress, trgAddress) ->
						XVM.set_double(trgAddress + trgOffset, XVM.get_char(srcAddress + srcOffset))
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createConverter_int(
				final int      srcOffset ,
				final Class<?> targetType,
				final int      trgOffset
			)
			{
				// super compact and still nicely breakpoint-debuggable.
				return targetType == byte.class
					? (srcAddress, trgAddress) ->
						XVM.set_byte(trgAddress + trgOffset, (byte)XVM.get_int(srcAddress + srcOffset))
					: targetType == boolean.class
					? (srcAddress, trgAddress) ->
						XVM.set_boolean(trgAddress + trgOffset, 0 != XVM.get_int(srcAddress + srcOffset))
					: targetType == short.class
					? (srcAddress, trgAddress) ->
						XVM.set_short(trgAddress + trgOffset, (short)XVM.get_int(srcAddress + srcOffset))
					: targetType == char.class
					? (srcAddress, trgAddress) ->
						XVM.set_char(trgAddress + trgOffset, (char)XVM.get_int(srcAddress + srcOffset))
					: targetType == int.class
					? Size4(srcOffset, trgOffset)
					: targetType == float.class
					? (srcAddress, trgAddress) ->
						XVM.set_float(trgAddress + trgOffset, XVM.get_int(srcAddress + srcOffset))
					: targetType == long.class
					? (srcAddress, trgAddress) ->
						XVM.set_long(trgAddress + trgOffset, XVM.get_int(srcAddress + srcOffset))
					: targetType == double.class
					? (srcAddress, trgAddress) ->
						XVM.set_double(trgAddress + trgOffset, XVM.get_int(srcAddress + srcOffset))
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createConverter_float(
				final int      srcOffset ,
				final Class<?> targetType,
				final int      trgOffset
			)
			{
				// super compact and still nicely breakpoint-debuggable.
				return targetType == byte.class
					? (srcAddress, trgAddress) ->
						XVM.set_byte(trgAddress + trgOffset, (byte)XVM.get_float(srcAddress + srcOffset))
					: targetType == boolean.class
					? (srcAddress, trgAddress) ->
						XVM.set_boolean(trgAddress + trgOffset, 0 != XVM.get_float(srcAddress + srcOffset))
					: targetType == short.class
					? (srcAddress, trgAddress) ->
						XVM.set_short(trgAddress + trgOffset, (short)XVM.get_float(srcAddress + srcOffset))
					: targetType == char.class
					? (srcAddress, trgAddress) ->
						XVM.set_char(trgAddress + trgOffset, (char)XVM.get_float(srcAddress + srcOffset))
					: targetType == int.class
					? (srcAddress, trgAddress) ->
						XVM.set_int(trgAddress + trgOffset, (int)XVM.get_float(srcAddress + srcOffset))
					: targetType == float.class
					? Size4(srcOffset, trgOffset)
					: targetType == long.class
					? (srcAddress, trgAddress) ->
						XVM.set_long(trgAddress + trgOffset, (long)XVM.get_float(srcAddress + srcOffset))
					: targetType == double.class
					? (srcAddress, trgAddress) ->
						XVM.set_double(trgAddress + trgOffset, XVM.get_float(srcAddress + srcOffset))
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createConverter_long(
				final int      srcOffset ,
				final Class<?> targetType,
				final int      trgOffset
			)
			{
				// super compact and still nicely breakpoint-debuggable.
				return targetType == byte.class
					? (srcAddress, trgAddress) ->
						XVM.set_byte(trgAddress + trgOffset, (byte)XVM.get_long(srcAddress + srcOffset))
					: targetType == boolean.class
					? (srcAddress, trgAddress) ->
						XVM.set_boolean(trgAddress + trgOffset, 0 != XVM.get_long(srcAddress + srcOffset))
					: targetType == short.class
					? (srcAddress, trgAddress) ->
						XVM.set_short(trgAddress + trgOffset, (short)XVM.get_long(srcAddress + srcOffset))
					: targetType == char.class
					? (srcAddress, trgAddress) ->
						XVM.set_char(trgAddress + trgOffset, (char)XVM.get_long(srcAddress + srcOffset))
					: targetType == int.class
					? (srcAddress, trgAddress) ->
						XVM.set_int(trgAddress + trgOffset, (int)XVM.get_long(srcAddress + srcOffset))
					: targetType == float.class
					? (srcAddress, trgAddress) ->
						XVM.set_float(trgAddress + trgOffset, XVM.get_long(srcAddress + srcOffset))
					: targetType == long.class
					? Size8(srcOffset, trgOffset)
					: targetType == double.class
					? Size8(srcOffset, trgOffset)
					: throwUnhandledPrimitiveTypeException(targetType)
				;
			}
			
			private static BinaryValueTranslator createConverter_double(
				final int      srcOffset ,
				final Class<?> targetType,
				final int      trgOffset
			)
			{
				// super compact and still nicely breakpoint-debuggable.
				return targetType == byte.class
					? (srcAddress, trgAddress) ->
						XVM.set_byte(trgAddress + trgOffset, (byte)XVM.get_double(srcAddress + srcOffset))
					: targetType == boolean.class
					? (srcAddress, trgAddress) ->
						XVM.set_boolean(trgAddress + trgOffset, 0 != XVM.get_double(srcAddress + srcOffset))
					: targetType == short.class
					? (srcAddress, trgAddress) ->
						XVM.set_short(trgAddress + trgOffset, (short)XVM.get_double(srcAddress + srcOffset))
					: targetType == char.class
					? (srcAddress, trgAddress) ->
						XVM.set_char(trgAddress + trgOffset, (char)XVM.get_double(srcAddress + srcOffset))
					: targetType == int.class
					? (srcAddress, trgAddress) ->
						XVM.set_int(trgAddress + trgOffset, (int)XVM.get_double(srcAddress + srcOffset))
					: targetType == float.class
					? (srcAddress, trgAddress) ->
						XVM.set_float(trgAddress + trgOffset, (float)XVM.get_double(srcAddress + srcOffset))
					: targetType == long.class
					? (srcAddress, trgAddress) ->
						XVM.set_long(trgAddress + trgOffset, (long)XVM.get_double(srcAddress + srcOffset))
					: targetType == double.class
					? (srcAddress, trgAddress) ->
						XVM.set_double(trgAddress + trgOffset, XVM.get_double(srcAddress + srcOffset))
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
	

	
	public static BinaryValueTranslator New(final int size, final int sourceOffset, final int targetOffset)
	{
		switch(size)
		{
			case 1: return Size1(sourceOffset, targetOffset);
			case 2: return Size2(sourceOffset, targetOffset);
			case 4: return Size4(sourceOffset, targetOffset);
			case 8: return Size8(sourceOffset, targetOffset);
			// (18.09.2018 TM)EXCP: proper exception
			default: throw new IllegalArgumentException("Illegal value byte size: " + size);
		}
	}
	
	public static BinaryValueTranslator.Size1 Size1(final int sourceOffset, final int targetOffset)
	{
		return new Size1(
			XMath.positive(sourceOffset),
			XMath.positive(targetOffset)
		);
	}
	
	public static BinaryValueTranslator.Size2 Size2(final int sourceOffset, final int targetOffset)
	{
		return new Size2(
			XMath.positive(sourceOffset),
			XMath.positive(targetOffset)
		);
	}
	
	public static BinaryValueTranslator.Size4 Size4(final int sourceOffset, final int targetOffset)
	{
		return new Size4(
			XMath.positive(sourceOffset),
			XMath.positive(targetOffset)
		);
	}
	
	public static BinaryValueTranslator.Size8 Size8(final int sourceOffset, final int targetOffset)
	{
		return new Size8(
			XMath.positive(sourceOffset),
			XMath.positive(targetOffset)
		);
	}
	
	public abstract class AbstractImplementation implements BinaryValueTranslator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final int sourceOffset;
		final int targetOffset;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		AbstractImplementation(final int sourceOffset, final int targetOffset)
		{
			super();
			this.sourceOffset = sourceOffset;
			this.targetOffset = targetOffset;
		}
		
	}
	
	public final class Size1 extends AbstractImplementation
	{
		Size1(final int sourceOffset, final int targetOffset)
		{
			super(sourceOffset, targetOffset);
		}

		@Override
		public final void translateValue(final long sourceAddress, final long targetAddress)
		{
			XVM.set_byte(targetAddress + this.targetOffset, XVM.get_byte(sourceAddress + this.sourceOffset));
		}
		
	}
	
	public final class Size2 extends AbstractImplementation
	{
		Size2(final int sourceOffset, final int targetOffset)
		{
			super(sourceOffset, targetOffset);
		}

		@Override
		public final void translateValue(final long sourceAddress, final long targetAddress)
		{
			XVM.set_short(targetAddress + this.targetOffset, XVM.get_short(sourceAddress + this.sourceOffset));
		}
		
	}
	
	public final class Size4 extends AbstractImplementation
	{
		Size4(final int sourceOffset, final int targetOffset)
		{
			super(sourceOffset, targetOffset);
		}

		@Override
		public final void translateValue(final long sourceAddress, final long targetAddress)
		{
			XVM.set_int(targetAddress + this.targetOffset, XVM.get_int(sourceAddress + this.sourceOffset));
		}
		
	}
	
	public final class Size8 extends AbstractImplementation
	{
		Size8(final int sourceOffset, final int targetOffset)
		{
			super(sourceOffset, targetOffset);
		}

		@Override
		public final void translateValue(final long sourceAddress, final long targetAddress)
		{
			XVM.set_long(targetAddress + this.targetOffset, XVM.get_long(sourceAddress + this.sourceOffset));
		}
		
	}
	
}
