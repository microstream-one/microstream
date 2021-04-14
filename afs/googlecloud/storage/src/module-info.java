module microstream.afs.googlecloud.storage
{
	exports one.microstream.afs.googlecloud.storage.types;
	
	requires com.google.auth;
	requires com.google.auth.oauth2;
	requires gax;
	requires google.cloud.core;
	requires google.cloud.storage;
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
}
