package one.microstream.afs.googlecloud.storage.types;

import java.util.regex.Pattern;

import one.microstream.afs.blobstore.types.BlobStorePath;

public interface GoogleCloudStoragePathValidator extends BlobStorePath.Validator
{

	public static GoogleCloudStoragePathValidator New()
	{
		return new GoogleCloudStoragePathValidator.Default();
	}


	public static class Default implements GoogleCloudStoragePathValidator
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
		 * https://cloud.google.com/storage/docs/naming-buckets
		 */
		void validateBucketName(
			final String bucketName
		)
		{
			final int length = bucketName.length();
			if(length < 3)
			{
				throw new IllegalArgumentException(
					"bucket name must be at least 3 characters long"
				);
			}
			if(bucketName.indexOf('.') == -1)
			{
				if(length > 63)
				{
					throw new IllegalArgumentException(
						"bucket name without dot cannot be longer than 63 characters"
					);
				}
			}
			else
			{
				if(length > 222)
				{
					throw new IllegalArgumentException(
						"bucket name containing a dot cannot be longer than 222 characters"
					);
				}
				for(final String part : bucketName.split("\\."))
				{
					if(part.length() > 63)
					{
						throw new IllegalArgumentException(
							"bucket name parts separated by dots cannot be longer than 63 characters"
						);
					}
				}
			}
			if(!Pattern.matches(
				"[a-z0-9_\\.\\-]*",
				bucketName
			))
			{
				throw new IllegalArgumentException(
					"bucket name can contain only lowercase letters, numbers, periods (.), underscores (_) and dashes (-)"
				);
			}
			if(!Pattern.matches("[a-z0-9]", bucketName.substring(0, 1))
			|| !Pattern.matches("[a-z0-9]", bucketName.substring(length - 1, length))
			)
			{
				throw new IllegalArgumentException(
					"bucket name must begin and end with a lowercase letters or a number"
				);
			}
			if(bucketName.contains(".."))
			{
				throw new IllegalArgumentException(
					"bucket name cannot have consecutive periods (..)"
				);
			}
			if(bucketName.contains(".-")
			|| bucketName.contains("-."))
			{
				throw new IllegalArgumentException(
					"bucket name cannot have dashes adjacent to periods (.- or -.)"
				);
			}
			if(Pattern.matches(
				"^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$",
				bucketName
			))
			{
				throw new IllegalArgumentException(
					"bucket name must not be in an IP address style"
				);
			}
			if(bucketName.startsWith("goog"))
			{
				throw new IllegalArgumentException(
					"bucket names must not start with 'goog'"
				);
			}
		}

	}

}
