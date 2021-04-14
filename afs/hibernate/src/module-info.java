module microstream.afs.hibernate
{
	exports one.microstream.afs.hibernate.types;
	
	requires java.naming;
	requires java.persistence;
	requires java.sql;
	requires microstream.afs;
	requires microstream.afs.sql;
	requires microstream.base;
	requires microstream.configuration;
	requires org.hibernate.orm.core;
}
