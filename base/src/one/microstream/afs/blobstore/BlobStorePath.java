package one.microstream.afs.blobstore;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import one.microstream.collections.XArrays;

public interface BlobStorePath
{
	public final static String SEPARATOR      = "/";
	public final static char   SEPARATOR_CHAR = '/';

	public String[] pathElements();

	public String container();

	public String identifier();

	public String fullQualifiedName();

	public BlobStorePath parentPath();


	public static BlobStorePath New(
		final String... pathElements
	)
	{
		if(pathElements.length == 0)
		{
			throw new IllegalArgumentException("empty path");
		}

		return new Default(pathElements);
	}


	public final static class Default implements BlobStorePath
	{
		private final String[] pathElements     ;
		private       String   fullQualifiedName;

		Default(
			final String[] pathElements
		)
		{
			super();
			this.pathElements = pathElements;
		}

		@Override
		public String[] pathElements()
		{
			return this.pathElements;
		}

		@Override
		public String container()
		{
			return this.pathElements[0];
		}

		@Override
		public String identifier()
		{
			return this.pathElements[this.pathElements.length - 1];
		}

		@Override
		public String fullQualifiedName()
		{
			if(this.fullQualifiedName == null)
			{
				this.fullQualifiedName = this.pathElements.length == 1
					? this.pathElements[0]
					: Arrays
						.stream(this.pathElements)
						.collect(joining(SEPARATOR))
				;
			}

			return this.fullQualifiedName;
		}

		@Override
		public BlobStorePath parentPath()
		{
			return this.pathElements.length > 1
				? new BlobStorePath.Default(
					XArrays.copyRange(this.pathElements, 0, this.pathElements.length - 1)
				)
				: null;
		}

	}

}
