package one.microstream.afs.kafka.types;

import java.util.Properties;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class KafkaFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public KafkaFileSystemCreator()
	{
		super(AFileSystem.class);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration kafkaConfiguration = configuration.child("kafka-properties");
		if(kafkaConfiguration == null)
		{
			return null;
		}
		
		final Properties     kafkaProperties = new Properties();
		kafkaProperties.putAll(kafkaConfiguration.coalescedMap());
		final boolean        cache           = configuration.optBoolean("cache").orElse(true);
		final KafkaConnector connector       = cache
			? KafkaConnector.Caching(kafkaProperties)
			: KafkaConnector.New(kafkaProperties)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
}
