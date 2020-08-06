package one.microstream.afs.coherence;

import static one.microstream.chars.XChars.notEmpty;
import static one.microstream.math.XMath.notNegative;

public interface BlobMetadata
{
	public String key();

	public long size();


	public static BlobMetadata New(
		final String key ,
		final long   size
	)
	{
		return new BlobMetadata.Default(
			notEmpty   (key ),
			notNegative(size)
		);
	}


	public static class Default implements BlobMetadata
	{
		private final String key ;
		private final long   size;

		Default(
			final String key ,
			final long   size
		)
		{
			super();
			this.key  = key ;
			this.size = size;
		}

		@Override
		public String key()
		{
			return this.key;
		}

		@Override
		public long size()
		{
			return this.size;
		}

	}

}
