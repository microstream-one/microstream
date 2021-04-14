package one.microstream.afs.azure.storage.types;

import java.util.regex.Pattern;

import one.microstream.afs.blobstore.types.BlobStorePath;

public interface AzureStoragePathValidator extends BlobStorePath.Validator
{

	public static AzureStoragePathValidator New()
	{
		return new AzureStoragePathValidator.Default();
	}


	public static class Default implements AzureStoragePathValidator
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
			this.validateContainerName(path.container());

		}

		/*
		 * https://github.com/MicrosoftDocs/azure-docs/blob/master/includes/storage-container-naming-rules-include.md
		 */
		void validateContainerName(
			final String bucketName
		)
		{
			final int length = bucketName.length();
			if(length < 3
			|| length > 63
			)
			{
				throw new IllegalArgumentException(
					"container name must be between 3 and 63 characters long"
				);
			}
			if(!Pattern.matches(
				"[a-z0-9\\-]*",
				bucketName
			))
			{
				throw new IllegalArgumentException(
					"container name can contain only lowercase letters, numbers and dashes (-)"
				);
			}
			if(!Pattern.matches(
				"[a-z0-9]",
				bucketName.substring(0, 1)
			))
			{
				throw new IllegalArgumentException(
					"bucket name must begin with a lowercase letters or a number"
				);
			}
			if(bucketName.endsWith("-"))
			{
				throw new IllegalArgumentException(
					"bucket name must not end with a dash (-)"
				);
			}
			if(bucketName.contains("--"))
			{
				throw new IllegalArgumentException(
					"bucket name cannot have consecutive dashes (--)"
				);
			}
		}

	}

}
