package one.microstream.storage.configuration;

import one.microstream.bytes.ByteMultiple;
import one.microstream.chars.ObjectStringAssembler;
import one.microstream.chars.VarString;

/**
 * Converts a byte size value from long into a human readable format.
 *
 * @see ByteMultiple
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
			final ByteMultiple[] byteMultiples =
			{
				ByteMultiple.KB,
				ByteMultiple.MB,
				ByteMultiple.GB,
				ByteMultiple.TB,
				ByteMultiple.PB,
				ByteMultiple.EB,
				ByteMultiple.ZB,
				ByteMultiple.YB
			};

			for(final ByteMultiple byteMultiple : byteMultiples)
			{
				final double value     = ByteMultiple.convert(byteSize, ByteMultiple.B).to(byteMultiple);
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
