module microstream.afs.azure.storage
{
	exports one.microstream.afs.azure.storage.types;
	
	requires com.azure.core;
	requires com.azure.storage.blob;
	requires com.azure.storage.common;
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
}
