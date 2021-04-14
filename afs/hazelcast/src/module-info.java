module microstream.afs.hazelcast
{
	exports one.microstream.afs.hazelcast.types;
	
	requires com.hazelcast.core;
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
}
