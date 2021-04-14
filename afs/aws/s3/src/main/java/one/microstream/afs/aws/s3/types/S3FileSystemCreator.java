package one.microstream.afs.aws.s3.types;

import one.microstream.afs.aws.types.AwsFileSystemCreator;
import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.types.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

public class S3FileSystemCreator extends AwsFileSystemCreator
{
	public S3FileSystemCreator()
	{
		super();
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration s3Configuration = configuration.child("aws.s3");
		if(s3Configuration == null)
		{
			return null;
		}
		
		final S3ClientBuilder clientBuilder = S3Client.builder();
		this.populateBuilder(clientBuilder, s3Configuration);
		
		final S3Client    client    = clientBuilder.build();
		final boolean     cache     = configuration.optBoolean("cache").orElse(true);
		final S3Connector connector = cache
			? S3Connector.Caching(client)
			: S3Connector.New(client)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
}
