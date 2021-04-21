package one.microstream.logging.flogger.types;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.FluentLogger.Api;
import com.google.common.flogger.LogContext.Key;
import com.google.common.flogger.context.Tags;

import one.microstream.logging.types.Level;
import one.microstream.logging.types.Logger;

public interface FloggerLogger extends Logger
{
	public static class Default implements FloggerLogger
	{
		static class FloggerChain implements Chain
		{
			private final Api api;

			FloggerChain(final Api api)
			{
				this.api = api;
			}

			@Override
			public FloggerChain withCause(
				final Throwable cause
			)
			{
				this.api.withCause(cause);
				return this;
			}
			
			@Override
			public Chain withTag(
				final String tag
			)
			{
				this.api.with(
					Key.TAGS,
					Tags.builder().addTag(tag).build()
				);
				return this;
			}

			@Override
			public void log(
				final String msg
			)
			{
				this.api.log(msg);
			}

			@Override
			public void log(
				final String msg,
				final Object p1
			)
			{
				this.api.log(msg, p1);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3
			)
			{
				this.api.log(msg, p1, p2, p3);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3,
				final Object p4
			)
			{
				this.api.log(msg, p1, p2, p3, p4);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3,
				final Object p4,
				final Object p5
			)
			{
				this.api.log(msg, p1, p2, p3, p4, p5);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3,
				final Object p4,
				final Object p5,
				final Object p6
			)
			{
				this.api.log(msg, p1, p2, p3, p4, p5, p6);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3,
				final Object p4,
				final Object p5,
				final Object p6,
				final Object p7
			)
			{
				this.api.log(msg, p1, p2, p3, p4, p5, p6, p7);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3,
				final Object p4,
				final Object p5,
				final Object p6,
				final Object p7,
				final Object p8
			)
			{
				this.api.log(msg, p1, p2, p3, p4, p5, p6, p7, p8);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3,
				final Object p4,
				final Object p5,
				final Object p6,
				final Object p7,
				final Object p8,
				final Object p9
			)
			{
				this.api.log(msg, p1, p2, p3, p4, p5, p6, p7, p8, p9);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3,
				final Object p4,
				final Object p5,
				final Object p6,
				final Object p7,
				final Object p8,
				final Object p9,
				final Object p10
			)
			{
				this.api.log(msg, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3,
				final Object p4,
				final Object p5,
				final Object p6,
				final Object p7,
				final Object p8,
				final Object p9,
				final Object p10,
				final Object... rest
			)
			{
				this.api.log(msg, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, rest);
			}

			@Override
			public void log(
				final String msg,
				final char p1
			)
			{
				this.api.log(msg, p1);
			}

			@Override
			public void log(
				final String msg,
				final byte p1
			)
			{
				this.api.log(msg, p1);
			}

			@Override
			public void log(
				final String msg,
				final short p1
			)
			{
				this.api.log(msg, p1);
			}

			@Override
			public void log(
				final String msg,
				final int p1
			)
			{
				this.api.log(msg, p1);
			}

			@Override
			public void log(
				final String msg,
				final long p1
			)
			{
				this.api.log(msg, p1);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final Object p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final boolean p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final char p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final byte p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final short p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final int p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final long p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final float p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final double p2
			)
			{
				this.api.log(msg, p1, p2);
			}
			
		}

		private static Api atLevel(final FluentLogger logger, final Level level)
		{
			switch(level)
			{
				case ERROR: return logger.atSevere();
				case WARN : return logger.atWarning() ;
				case INFO : return logger.atInfo();
				case CONF : return logger.atConfig();
				case DEBUG: return logger.atFine();
				case TRACE: return logger.atFinest();
				default   : throw new IllegalArgumentException();
			}
		}


		private final FluentLogger flogger;

		Default(final FluentLogger flogger)
		{
			this.flogger = flogger;
		}

		@Override
		public Chain atLevel(final Level level)
		{
			return new FloggerChain(atLevel(this.flogger, level));
		}

		@Override
		public boolean isEnabled(final Level level)
		{
			return atLevel(this.flogger, level).isEnabled();
		}

	}

}
