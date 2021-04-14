module microstream.afs.oracle.nosql
{
	exports one.microstream.afs.oracle.nosql.types;
	
	requires java.sql;
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
	requires oracle.nosql.client;
}
