package one.microstream.afs.mongodb.types;

import one.microstream.afs.blobstore.types.BlobStorePath;

public interface MongoDbPathValidator extends BlobStorePath.Validator
{

	public static MongoDbPathValidator New()
	{
		return new MongoDbPathValidator.Default();
	}


	public static class Default implements MongoDbPathValidator
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
			this.validateCollectionName(path.container());

		}

		/*
		 * https://docs.mongodb.com/manual/reference/limits/#naming-restrictions
		 */
		void validateCollectionName(
			final String collectionName
		)
		{
			if(collectionName.contains("$"))
			{
				throw new IllegalArgumentException(
					"collection name cannot contain the dollar sign"
				);
			}
			if(collectionName.startsWith("system."))
			{
				throw new IllegalArgumentException(
					"collection name cannot begin with 'system.'"
				);
			}
		}

	}

}
