package one.microstream.storage.configuration;

import one.microstream.bytes.ByteMultiple;

/**
 * @deprecated Replaced by {@link ByteSizeParser}, will be removed in a future release.
 */
@Deprecated
public interface FileSizeParser extends ByteSizeParser
{
	public long parseFileSize(String text, ByteMultiple defaultByteMultiple);


	public static FileSizeParser Default()
	{
		return new FileSizeParser.Default();
	}


	public static class Default extends ByteSizeParser.Default implements FileSizeParser
	{
		Default()
		{
			super();
		}

		@Override
		public long parseFileSize(
			final String       text               ,
			final ByteMultiple defaultByteMultiple
		)
		{
			return super.parseByteSize(text, defaultByteMultiple);
		}

	}

}
