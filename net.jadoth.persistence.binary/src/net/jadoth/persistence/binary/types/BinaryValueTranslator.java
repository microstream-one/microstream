package net.jadoth.persistence.binary.types;

import net.jadoth.low.XVM;
import net.jadoth.math.XMath;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;

public interface BinaryValueTranslator
{
	public void translateValue(long sourceAddress, long targetAddress);
	
	@FunctionalInterface
	public interface Creator
	{
		public BinaryValueTranslator createValueTranslator(
			PersistenceTypeDescriptionMember sourceMember      ,
			int                              sourceMemberOffset,
			PersistenceTypeDescriptionMember targetMember      ,
			int                              targetMemberOffset
		);
		
		public static BinaryValueTranslator.Creator New()
		{
			return new BinaryValueTranslator.Creator.Implementation();
		}
		
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
				if(sourceMember.isReference())
				{
					if(!targetMember.isReference())
					{
						// (18.09.2018 TM)EXCP: proper exception
						throw new RuntimeException("Cannot convert between primitive and reference values.");
					}
					
					// all references are stored as OID primitive values
					return BinaryValueTranslator.New((int)BinaryPersistence.oidLength(), sourceMemberOffset, targetMemberOffset);
				}

				// (18.09.2018 TM)FIXME: support converting between primitive values
				if(!sourceMember.typeName().equals(targetMember.typeName()))
				{
					throw new RuntimeException("Converting between primitive values of different type is not supported, yet");
				}
				
				return BinaryValueTranslator.New((int)sourceMember.persistentMaximumLength(), sourceMemberOffset, targetMemberOffset);
			}
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
	
	public static BinaryValueTranslator New(final int size, final int sourceOffset, final int targetOffset)
	{
		switch(size)
		{
			case 1: return Size1(sourceOffset, targetOffset);
			case 2: return Size2(sourceOffset, targetOffset);
			case 4: return Size4(sourceOffset, targetOffset);
			case 8: return Size8(sourceOffset, targetOffset);
			// (18.09.2018 TM)EXCP: proper exception
			default: throw new IllegalArgumentException("Illegal primitive value byte size: " + size);
		}
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
