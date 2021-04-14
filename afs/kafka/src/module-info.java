module microstream.afs.kafka
{
	exports one.microstream.afs.kafka.types;
	
	requires kafka.clients;
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
	requires org.slf4j;
}
