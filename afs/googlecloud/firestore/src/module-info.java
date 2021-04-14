module microstream.afs.googlecloud.firestore
{
	exports one.microstream.afs.googlecloud.firestore.types;
	
	requires com.google.api.apicommon;
	requires com.google.auth;
	requires com.google.auth.oauth2;
	requires com.google.protobuf;
	requires gax;
	requires google.cloud.core;
	requires google.cloud.firestore;
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
}
