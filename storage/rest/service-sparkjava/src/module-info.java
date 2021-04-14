module microstream.storage.restservice.sparkjava
{
	exports one.microstream.storage.restservice.sparkjava.exceptions;
	exports one.microstream.storage.restservice.sparkjava.types;
	
	requires com.google.gson;
	requires microstream.base;
	requires microstream.persistence;
	requires microstream.storage;
	requires microstream.storage.restadapter;
	requires microstream.storage.restservice;
	requires spark.core;
}
