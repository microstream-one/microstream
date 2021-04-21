package one.microstream.logging.zero.types;

import com.obsidiandynamics.zerolog.LogLevel;
import com.obsidiandynamics.zerolog.Zlg;
import com.obsidiandynamics.zerolog.Zlg.LogChain;

import one.microstream.logging.types.Level;
import one.microstream.logging.types.Logger;

public interface ZeroLogger extends Logger
{
	public static class Default implements ZeroLogger
	{
		static class ZeroChain implements Chain
		{
			private final LogChain chain;

			ZeroChain(final LogChain chain)
			{
				this.chain = chain;
			}

			@Override
			public Chain withCause(
				final Throwable cause
			)
			{
				this.chain.threw(cause);
				return this;
			}

			@Override
			public Chain withTag(
				final String tag
			)
			{
				this.chain.tag(tag);
				return this;
			}
			
			@Override
			public void log(
				final String msg
			)
			{
				this.chain
					.message(msg)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final Object p2,
				final Object p3
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.log()
				;
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
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.arg(p4)
					.log()
				;
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
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.arg(p4)
					.arg(p5)
					.log()
				;
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
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.arg(p4)
					.arg(p5)
					.arg(p6)
					.log()
				;
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
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.arg(p4)
					.arg(p5)
					.arg(p6)
					.arg(p7)
					.log()
				;
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
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.arg(p4)
					.arg(p5)
					.arg(p6)
					.arg(p7)
					.arg(p8)
					.log()
				;
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
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.arg(p4)
					.arg(p5)
					.arg(p6)
					.arg(p7)
					.arg(p8)
					.arg(p9)
					.log()
				;
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
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.arg(p4)
					.arg(p5)
					.arg(p6)
					.arg(p7)
					.arg(p8)
					.arg(p9)
					.arg(p10)
					.log()
				;
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
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.arg(p3)
					.arg(p4)
					.arg(p5)
					.arg(p6)
					.arg(p7)
					.arg(p8)
					.arg(p9)
					.arg(p10)
				;
				for(final Object p : rest)
				{
					this.chain.arg(p);
				}
				this.chain.log();
			}

			@Override
			public void log(
				final String msg,
				final char p1
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final Object p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final Object p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final boolean p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final char p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final byte p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final short p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final int p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final long p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final float p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final boolean p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final char p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final byte p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final short p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final int p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final long p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final float p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			@Override
			public void log(
				final String msg,
				final double p1,
				final double p2
			)
			{
				this.chain
					.message(msg)
					.arg(p1)
					.arg(p2)
					.log()
				;
			}

			
			
		}

		private static int zeroLevel(final Level level)
		{
			switch(level)
			{
				case ERROR: return LogLevel.ERROR;
				case WARN : return LogLevel.WARN ;
				case INFO : return LogLevel.INFO ;
				case CONF : return LogLevel.CONF ;
				case DEBUG: return LogLevel.DEBUG;
				case TRACE: return LogLevel.TRACE;
				default   : throw new IllegalArgumentException();
			}
		}


		private final Zlg zlg;

		Default(final Zlg zlg)
		{
			this.zlg = zlg;
		}

		@Override
		public Chain atLevel(final Level level)
		{
			return new ZeroChain(this.zlg.level(zeroLevel(level)));
		}

		@Override
		public boolean isEnabled(final Level level)
		{
			return this.zlg.isEnabled(zeroLevel(level));
		}

	}

}
