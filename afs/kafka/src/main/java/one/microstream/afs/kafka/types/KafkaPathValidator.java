package one.microstream.afs.kafka.types;

import java.util.regex.Pattern;

import one.microstream.afs.blobstore.types.BlobStorePath;

public interface KafkaPathValidator extends BlobStorePath.Validator
{

	public static KafkaPathValidator New()
	{
		return new KafkaPathValidator.Default();
	}


	public static class Default implements KafkaPathValidator
	{
		Default()
		{
			super();
		}

		/*
		 * https://stackoverflow.com/questions/37062904/what-are-apache-kafka-topic-name-limitations
		 */
		@Override
		public void validate(
			final BlobStorePath path
		)
		{
			final String name = path.fullQualifiedName().replace(BlobStorePath.SEPARATOR_CHAR, '_');
			if(name.length() > 249)
			{
				throw new IllegalArgumentException(
					"full qualified path name cannot be longer than 249 characters"
				);
			}
			if(!Pattern.matches(
				"[a-zA-Z0-9\\._\\-]*",
				name
			))
			{
				throw new IllegalArgumentException(
					"path can contain only letters, numbers, periods (.), underscores (_) and dashes (-)"
				);
			}
		}

	}

}
