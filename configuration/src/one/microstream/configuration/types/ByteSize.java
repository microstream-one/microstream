package one.microstream.configuration.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

public interface ByteSize extends Comparable<ByteSize>
{
	public double amount();
	
	public ByteUnit unit();
	
	public long bytes();
	
	
	public static ByteSize New(
		final double   amount,
		final ByteUnit unit
	)
	{
		return new Default(
			notNegative(amount),
			notNull(unit)
		);
	}
	
	
	public static class Default implements ByteSize
	{
		private final double   amount;
		private final ByteUnit unit  ;
		private final long     bytes ;
		
		Default(
			final double   amount,
			final ByteUnit unit
		)
		{
			super();
			this.amount = amount              ;
			this.unit   = unit                ;
			this.bytes  = unit.toBytes(amount);
		}
		
		@Override
		public double amount()
		{
			return this.amount;
		}
		
		@Override
		public ByteUnit unit()
		{
			return this.unit;
		}
		
		@Override
		public long bytes()
		{
			return this.bytes;
		}
		
		@Override
		public int compareTo(
			final ByteSize other
		)
		{
			return Long.compare(this.bytes, other.bytes());
		}
		
		@Override
		public int hashCode()
		{
			return Long.hashCode(this.bytes);
		}
		
		@Override
		public boolean equals(
			final Object obj
		)
		{
			if(obj == this)
			{
				return true;
			}
			
			if(!(obj instanceof ByteSize))
			{
				return false;
			}
			
			final ByteSize other = (ByteSize)obj;
			return this.bytes == other.bytes();
		}
		
	}
	
}
