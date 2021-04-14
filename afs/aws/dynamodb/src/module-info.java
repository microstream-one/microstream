module microstream.afs.aws.dynamodb
{
	exports one.microstream.afs.aws.dynamodb.types;
	
	requires microstream.afs;
	requires microstream.afs.aws;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
	requires software.amazon.awssdk.awscore;
	requires software.amazon.awssdk.core;
	requires software.amazon.awssdk.services.dynamodb;
	requires software.amazon.awssdk.utils;
}
