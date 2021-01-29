package one.microstream.storage.configuration;

import one.microstream.chars.ObjectStringAssembler;
import one.microstream.chars.VarString;
import one.microstream.configuration.types.ByteUnit;

/**
 * Converts a byte size value from long into a human readable format.
 *
 * @see ByteUnit
 * @since 3.1
 *
 */
public interface ByteSizeAssembler extends ObjectStringAssembler<Long>
{
	@Override
	public VarString assemble(VarString vs, Long byteSize);

	@Override
	public default String assemble(final Long byteSize)
	{
		return ObjectStringAssembler.super.assemble(byteSize);
	}


	public static ByteSizeAssembler Default()
	{
		return new ByteSizeAssembler.Default();
	}


	public static class Default implements ByteSizeAssembler
	{
		Default()
		{
			super();
		}

		@Override
		public VarString assemble(final VarString vs, final Long byteSize)
		{
			final ByteUnit[] byteMultiples =
			{
				ByteUnit.KB,
				ByteUnit.MB,
				ByteUnit.GB,
				ByteUnit.TB,
				ByteUnit.PB,
				ByteUnit.EB,
				ByteUnit.ZB,
				ByteUnit.YB
			};

			for(final ByteUnit byteMultiple : byteMultiples)
			{
				final double value     = ByteUnit.convert(byteSize, ByteUnit.B).to(byteMultiple);
				final long   longValue = (long)value;
				if(longValue > 0 && longValue < 1000)
				{
					if(longValue == value)
					{
						vs.add(longValue);
					}
					else
					{
						vs.add(value);
					}
					return vs.add(byteMultiple.name().toLowerCase());
				}
			}

			return vs.add(byteSize.longValue());
		}

	}

}
