module microstream.cache.hibernate
{
	exports one.microstream.cache.hibernate.types;
	
	requires cache.api;
	requires java.naming;
	requires java.persistence;
	requires microstream.base;
	requires microstream.cache;
	requires microstream.configuration;
	requires microstream.storage.embedded;
	requires org.hibernate.orm.core;
}
