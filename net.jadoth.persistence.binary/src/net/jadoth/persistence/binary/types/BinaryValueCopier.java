package net.jadoth.persistence.binary.types;

import net.jadoth.low.XVM;
import net.jadoth.math.XMath;

public interface BinaryValueCopier
{
	public void copy(long sourceAddress, long targetAddress);
	
	
	
	public static BinaryValueCopier.Size1 Size1(final int sourceOffset, final int targetOffset)
	{
		return new Size1(
			XMath.positive(sourceOffset),
			XMath.positive(targetOffset)
		);
	}
	
	public static BinaryValueCopier.Size2 Size2(final int sourceOffset, final int targetOffset)
	{
		return new Size2(
			XMath.positive(sourceOffset),
			XMath.positive(targetOffset)
		);
	}
	
	public static BinaryValueCopier.Size4 Size4(final int sourceOffset, final int targetOffset)
	{
		return new Size4(
			XMath.positive(sourceOffset),
			XMath.positive(targetOffset)
		);
	}
	
	public static BinaryValueCopier.Size8 Size8(final int sourceOffset, final int targetOffset)
	{
		return new Size8(
			XMath.positive(sourceOffset),
			XMath.positive(targetOffset)
		);
	}
	
	public abstract class AbstractImplementation implements BinaryValueCopier
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
		public final void copy(final long sourceAddress, final long targetAddress)
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
		public final void copy(final long sourceAddress, final long targetAddress)
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
		public final void copy(final long sourceAddress, final long targetAddress)
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
		public final void copy(final long sourceAddress, final long targetAddress)
		{
			XVM.set_long(targetAddress + this.targetOffset, XVM.get_long(sourceAddress + this.sourceOffset));
		}
		
	}
	
}
