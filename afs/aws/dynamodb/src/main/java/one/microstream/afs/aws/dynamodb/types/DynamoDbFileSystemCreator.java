package one.microstream.afs.aws.dynamodb.types;

import one.microstream.afs.aws.types.AwsFileSystemCreator;
import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.types.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

public class DynamoDbFileSystemCreator extends AwsFileSystemCreator
{
	public DynamoDbFileSystemCreator()
	{
		super();
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration dynamoConfiguration = configuration.child("aws.dynamodb");
		if(dynamoConfiguration == null)
		{
			return null;
		}
		
		final DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder();
		this.populateBuilder(clientBuilder, dynamoConfiguration);
		
		final DynamoDbClient    client    = clientBuilder.build();
		final boolean           cache     = configuration.optBoolean("cache").orElse(true);
		final DynamoDbConnector connector = cache
			? DynamoDbConnector.Caching(client)
			: DynamoDbConnector.New(client)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
}
