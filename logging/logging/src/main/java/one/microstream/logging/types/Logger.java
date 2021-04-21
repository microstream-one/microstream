package one.microstream.logging.types;

public interface Logger
{
	public static interface Chain
	{
		public Chain withCause(Throwable cause);
		
		public Chain withTag(String tag);
		
		public void log(String msg);
		
		public void log(String msg, Object p1);
		
		public void log(String msg, Object p1, Object p2);
		
		public void log(String msg, Object p1, Object p2, Object p3);
		
		public void log(String msg, Object p1, Object p2, Object p3, Object p4);
				
		public void log(String msg, Object p1, Object p2, Object p3, Object p4, Object p5);
		
		public void log(String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6);
		
		public void log(String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7);
		
		public void log(String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8);
		
		public void log(String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9);
		
		public void log(String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10);
		
		public void log(String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object... rest);
		
		public void log(String msg, char p1);
		
		public void log(String msg, byte p1);
		
		public void log(String msg, short p1);
		
		public void log(String msg, int p1);
		
		public void log(String msg, long p1);
		
		public void log(String msg, Object p1, boolean p2);
		
		public void log(String msg, Object p1, char p2);
		
		public void log(String msg, Object p1, byte p2);
		
		public void log(String msg, Object p1, short p2);
		
		public void log(String msg, Object p1, int p2);
		
		public void log(String msg, Object p1, long p2);
		
		public void log(String msg, Object p1, float p2);
		
		public void log(String msg, Object p1, double p2);
		
		public void log(String msg, boolean p1, Object p2);
		
		public void log(String msg, char p1, Object p2);
		
		public void log(String msg, byte p1, Object p2);
		
		public void log(String msg, short p1, Object p2);
		
		public void log(String msg, int p1, Object p2);
		
		public void log(String msg, long p1, Object p2);
		
		public void log(String msg, float p1, Object p2);
		
		public void log(String msg, double p1, Object p2);
		
		public void log(String msg, boolean p1, boolean p2);
		
		public void log(String msg, char p1, boolean p2);
		
		public void log(String msg, byte p1, boolean p2);
		
		public void log(String msg, short p1, boolean p2);
		
		public void log(String msg, int p1, boolean p2);
		
		public void log(String msg, long p1, boolean p2);
		
		public void log(String msg, float p1, boolean p2);
		
		public void log(String msg, double p1, boolean p2);
		
		public void log(String msg, boolean p1, char p2);
		
		public void log(String msg, char p1, char p2);
		
		public void log(String msg, byte p1, char p2);
		
		public void log(String msg, short p1, char p2);
		
		public void log(String msg, int p1, char p2);
		
		public void log(String msg, long p1, char p2);
		
		public void log(String msg, float p1, char p2);
		
		public void log(String msg, double p1, char p2);
		
		public void log(String msg, boolean p1, byte p2);
		
		public void log(String msg, char p1, byte p2);
		
		public void log(String msg, byte p1, byte p2);
		
		public void log(String msg, short p1, byte p2);
		
		public void log(String msg, int p1, byte p2);
		
		public void log(String msg, long p1, byte p2);
		
		public void log(String msg, float p1, byte p2);
		
		public void log(String msg, double p1, byte p2);
		
		public void log(String msg, boolean p1, short p2);
		
		public void log(String msg, char p1, short p2);
		
		public void log(String msg, byte p1, short p2);
		
		public void log(String msg, short p1, short p2);
		
		public void log(String msg, int p1, short p2);
		
		public void log(String msg, long p1, short p2);
		
		public void log(String msg, float p1, short p2);
		
		public void log(String msg, double p1, short p2);
		
		public void log(String msg, boolean p1, int p2);
		
		public void log(String msg, char p1, int p2);
		
		public void log(String msg, byte p1, int p2);
		
		public void log(String msg, short p1, int p2);
		
		public void log(String msg, int p1, int p2);
		
		public void log(String msg, long p1, int p2);
		
		public void log(String msg, float p1, int p2);
		
		public void log(String msg, double p1, int p2);
		
		public void log(String msg, boolean p1, long p2);
		
		public void log(String msg, char p1, long p2);
		
		public void log(String msg, byte p1, long p2);
		
		public void log(String msg, short p1, long p2);
		
		public void log(String msg, int p1, long p2);
		
		public void log(String msg, long p1, long p2);
		
		public void log(String msg, float p1, long p2);
		
		public void log(String msg, double p1, long p2);
		
		public void log(String msg, boolean p1, float p2);
		
		public void log(String msg, char p1, float p2);
		
		public void log(String msg, byte p1, float p2);
		
		public void log(String msg, short p1, float p2);
		
		public void log(String msg, int p1, float p2);
		
		public void log(String msg, long p1, float p2);
		
		public void log(String msg, float p1, float p2);
		
		public void log(String msg, double p1, float p2);
		
		public void log(String msg, boolean p1, double p2);
		
		public void log(String msg, char p1, double p2);
		
		public void log(String msg, byte p1, double p2);
		
		public void log(String msg, short p1, double p2);
		
		public void log(String msg, int p1, double p2);
		
		public void log(String msg, long p1, double p2);
		
		public void log(String msg, float p1, double p2);
		
		public void log(String msg, double p1, double p2);
		
	}

	public Chain atLevel(Level level);

	public boolean isEnabled(Level level);

	public default Chain error()
	{
		return this.atLevel(Level.ERROR);
	}

	public default Chain warn()
	{
		return this.atLevel(Level.WARN);
	}

	public default Chain info()
	{
		return this.atLevel(Level.INFO);
	}

	public default Chain conf()
	{
		return this.atLevel(Level.CONF);
	}

	public default Chain debug()
	{
		return this.atLevel(Level.DEBUG);
	}

	public default Chain trace()
	{
		return this.atLevel(Level.TRACE);
	}

}
