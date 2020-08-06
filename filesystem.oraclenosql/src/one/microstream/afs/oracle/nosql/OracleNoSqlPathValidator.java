package one.microstream.afs.oracle.nosql;

import java.util.regex.Pattern;

import one.microstream.afs.blobstore.BlobStorePath;

public interface OracleNoSqlPathValidator extends BlobStorePath.Validator
{

	public static OracleNoSqlPathValidator New()
	{
		return new OracleNoSqlPathValidator.Default();
	}


	public static class Default implements OracleNoSqlPathValidator
	{
		Default()
		{
			super();
		}

		@Override
		public void validate(
			final BlobStorePath path
		)
		{
			this.validateTableName(path.container());

		}

		/*
		 * https://docs.oracle.com/en/database/other-databases/nosql-database/20.1/java-driver-table/name-constraints.html
		 */
		void validateTableName(
			final String tableName
		)
		{
			final int length = tableName.length();
			if(length > 32
			)
			{
				throw new IllegalArgumentException(
					"table name cannot be longer than 32 characters"
				);
			}
			if(!Pattern.matches(
				"[a-zA-Z0-9_\\.]*",
				tableName
			))
			{
				throw new IllegalArgumentException(
					"table name can contain letters, numbers, periods (.) and underscores (_)"
				);
			}
			if(!Pattern.matches(
				"[a-zA-Z]",
				tableName.substring(0, 1)
			))
			{
				throw new IllegalArgumentException(
					"table names must start with a letter"
				);
			}
		}

	}

}
