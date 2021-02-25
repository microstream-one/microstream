package one.microstream.storage.configuration;

import java.time.Duration;

import one.microstream.chars.ObjectStringAssembler;
import one.microstream.chars.VarString;
import one.microstream.configuration.types.DurationUnit;

/**
 *
 * @deprecated will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
public interface DurationAssembler extends ObjectStringAssembler<Duration>
{
	@Override
	public VarString assemble(VarString vs, Duration duration);

	@Override
	public default String assemble(final Duration duration)
	{
		return ObjectStringAssembler.super.assemble(duration);
	}


	public static DurationAssembler IsoAssembler()
	{
		return (vs, duration) -> vs.append(duration.toString());
	}

	public static DurationAssembler Default()
	{
		return new DurationAssembler.Default();
	}


	public static class Default implements DurationAssembler
	{
		Default()
		{
			super();
		}

		@Override
		public VarString assemble(
			final VarString vs      ,
			final Duration  duration
		)
		{
			long value;
			if((value = duration.toDays()) > 0)
			{
				vs.add(value).add(DurationUnit.D.name());
			}
			else if((value = duration.toHours()) > 0)
			{
				vs.add(value).add(DurationUnit.H.name());
			}
			else if((value = duration.toMinutes()) > 0)
			{
				vs.add(value).add(DurationUnit.M.name());
			}
			else if((value = duration.getSeconds()) > 0)
			{
				vs.add(value).add(DurationUnit.S.name());
			}
			else if((value = duration.toMillis()) > 0)
			{
				vs.add(value).add(DurationUnit.MS.name());
			}
			else
			{
				vs.add(duration.toNanos()).add(DurationUnit.MS.name());
			}

			return vs;
		}

	}

}
