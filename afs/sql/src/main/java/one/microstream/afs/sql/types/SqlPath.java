package one.microstream.afs.sql.types;

import static java.util.stream.Collectors.joining;
import static one.microstream.X.notNull;

import java.util.Arrays;

import one.microstream.chars.XChars;
import one.microstream.collections.XArrays;

public interface SqlPath
{
	final static String DIRECTORY_TABLE_NAME_SEPARATOR      = "_";
	final static char   DIRECTORY_TABLE_NAME_SEPARATOR_CHAR = '_';

	public String[] pathElements();

	public String identifier();

	public String fullQualifiedName();

	public SqlPath parentPath();


	public static String[] splitPath(
		final String fullQualifiedPath
	)
	{
		return XChars.splitSimple(fullQualifiedPath, DIRECTORY_TABLE_NAME_SEPARATOR);
	}

	public static SqlPath New(
		final String... pathElements
	)
	{
		return new Default(
			notNull(pathElements)
		);
	}


	public final static class Default implements SqlPath
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
		public String identifier()
		{
			return this.pathElements[this.pathElements.length - 1];
		}

		@Override
		public String fullQualifiedName()
		{
			if(this.fullQualifiedName == null)
			{
				this.fullQualifiedName = Arrays
					.stream(this.pathElements)
					.collect(joining(DIRECTORY_TABLE_NAME_SEPARATOR))
				;
			}

			return this.fullQualifiedName;
		}

		@Override
		public SqlPath parentPath()
		{
			return this.pathElements.length > 1
				? new SqlPath.Default(
					XArrays.copyRange(this.pathElements, 0, this.pathElements.length - 1)
				)
				: null;
		}

	}

}
