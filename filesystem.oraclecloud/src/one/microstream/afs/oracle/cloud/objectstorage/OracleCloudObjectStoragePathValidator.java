package one.microstream.afs.oracle.cloud.objectstorage;

import java.util.regex.Pattern;

import one.microstream.afs.blobstore.BlobStorePath;

public interface OracleCloudObjectStoragePathValidator extends BlobStorePath.Validator
{

	public static OracleCloudObjectStoragePathValidator New()
	{
		return new OracleCloudObjectStoragePathValidator.Default();
	}


	public static class Default implements OracleCloudObjectStoragePathValidator
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
			this.validateBucketName(path.container());

		}

		/*
		 * No documentation found for bucket naming limitations.
		 * This is just the check taken from the console web interface.
		 */
		void validateBucketName(
			final String bucketName
		)
		{
			if(!Pattern.matches(
				"[a-zA-Z0-9_\\-]*",
				bucketName
			))
			{
				throw new IllegalArgumentException(
					"bucket name can contain only letters, numbers, underscores (_) and dashes (-)"
				);
			}
		}

	}

}
