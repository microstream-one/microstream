module mongodb
{
	exports one.microstream.afs.mongodb.types;
	
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
	requires org.mongodb.bson;
	requires org.mongodb.driver.core;
	requires org.mongodb.driver.sync.client;
}
