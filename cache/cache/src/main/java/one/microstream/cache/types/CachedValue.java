
package one.microstream.cache.types;

public interface CachedValue
{
	public long creationTime();
	
	public long accessTime();
	
	public long accessCount();
	
	public long modificationTime();
	
	public long modificationCount();
	
	public long expiryTime();
	
	public CachedValue expiryTime(long expiryTime);
	
	public boolean isExpiredAt(long now);
	
	public Object value();
	
	public Object value(long accessTime);
	
	public CachedValue value(Object value);
	
	public CachedValue value(Object value, long accessTime);
	
	public long byteSizeEstimate();
	
	public static CachedValue New(final Object value, final long creationTime, final long expiryTime)
	{
		return new Default(value, creationTime, expiryTime);
	}
	
	public static class Default implements CachedValue
	{
		private Object     value;
		private final long creationTime;
		private long       accessTime;
		private long       accessCount;
		private long       modificationTime;
		private long       modificationCount;
		private long       expiryTime;
		
		Default(final Object value, final long creationTime, final long expiryTime)
		{
			super();
			
			this.value             = value;
			this.creationTime      = creationTime;
			this.accessTime        = creationTime;
			this.modificationTime  = creationTime;
			this.expiryTime        = expiryTime;
			this.accessCount       = 0;
			this.modificationCount = 0;
		}
		
		@Override
		public long creationTime()
		{
			return this.creationTime;
		}
		
		@Override
		public long accessTime()
		{
			return this.accessTime;
		}
		
		@Override
		public long accessCount()
		{
			return this.accessCount;
		}
		
		@Override
		public long modificationTime()
		{
			return this.modificationTime;
		}
		
		@Override
		public long modificationCount()
		{
			return this.modificationCount;
		}
		
		@Override
		public long expiryTime()
		{
			return this.expiryTime;
		}
		
		@Override
		public CachedValue expiryTime(final long expiryTime)
		{
			this.expiryTime = expiryTime;
			return this;
		}
		
		@Override
		public boolean isExpiredAt(final long now)
		{
			final long expiryTime = this.expiryTime;
			return expiryTime > -1 && expiryTime <= now;
		}
		
		@Override
		public Object value()
		{
			return this.value;
		}
		
		@Override
		public Object value(final long accessTime)
		{
			this.accessTime = accessTime;
			this.accessCount++;
			return this.value;
		}
		
		@Override
		public CachedValue value(final Object value)
		{
			this.value = value;
			return this;
		}
		
		@Override
		public CachedValue value(final Object value, final long modificationTime)
		{
			this.modificationTime = modificationTime;
			this.value            = value;
			this.modificationCount++;
			return this;
		}
		
		@Override
		public long byteSizeEstimate()
		{
			return this.value instanceof ByteSized
				? ((ByteSized)this.value).byteSize()
				: -1;
		}
		
	}
	
}
