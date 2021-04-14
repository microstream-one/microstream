module microstream.afs.aws
{
	exports one.microstream.afs.aws.types;
	
	requires microstream.afs;
	requires microstream.base;
	requires microstream.configuration;
	requires software.amazon.awssdk.auth;
	requires software.amazon.awssdk.awscore;
	requires software.amazon.awssdk.core;
	requires software.amazon.awssdk.regions;
	requires software.amazon.awssdk.utils;
}
