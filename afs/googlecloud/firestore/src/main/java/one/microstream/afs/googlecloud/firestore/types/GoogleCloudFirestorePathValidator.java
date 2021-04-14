package one.microstream.afs.googlecloud.firestore.types;

import java.util.regex.Pattern;

import one.microstream.afs.blobstore.types.BlobStorePath;

public interface GoogleCloudFirestorePathValidator extends BlobStorePath.Validator
{

	public static GoogleCloudFirestorePathValidator New()
	{
		return new GoogleCloudFirestorePathValidator.Default();
	}


	public static class Default implements GoogleCloudFirestorePathValidator
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
		 * https://firebase.google.com/docs/firestore/quotas#collections_documents_and_fields
		 */
		void validateCollectionName(
			final String collectionName
		)
		{
			if(collectionName.indexOf('/') != -1)
			{
				throw new IllegalArgumentException(
					"collection name cannot contain a forward slash (/)"
				);
			}
			if(collectionName.equals(".")
			|| collectionName.equals(".."))
			{
				throw new IllegalArgumentException(
					"collection name cannot solely consist of a single period (.) or double periods (..)"
				);
			}
			if(Pattern.matches(
				"__.*__",
				collectionName
			))
			{
				throw new IllegalArgumentException(
					"collection name cannot match the regular expression __.*__"
				);
			}
		}

	}

}
