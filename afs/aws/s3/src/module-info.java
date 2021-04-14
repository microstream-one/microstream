module microstream.afs.aws.s3
{
	exports one.microstream.afs.aws.s3.types;
	
	requires microstream.afs;
	requires microstream.afs.aws;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
	requires software.amazon.awssdk.awscore;
	requires software.amazon.awssdk.core;
	requires software.amazon.awssdk.http;
	requires software.amazon.awssdk.services.s3;
	requires software.amazon.awssdk.utils;
}
