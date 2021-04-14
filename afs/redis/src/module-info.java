module microstream.afs.redis
{
	exports one.microstream.afs.redis.types;
	
	requires io.netty.buffer;
	requires io.netty.common;
	requires lettuce.core;
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
}
