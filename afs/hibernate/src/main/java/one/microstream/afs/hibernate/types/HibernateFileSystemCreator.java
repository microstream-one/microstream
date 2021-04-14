package one.microstream.afs.hibernate.types;

import java.util.HashMap;
import java.util.Map;

import one.microstream.afs.sql.types.SqlConnector;
import one.microstream.afs.sql.types.SqlFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class HibernateFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public HibernateFileSystemCreator()
	{
		super(AFileSystem.class);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration hibernateConfiguration = configuration.child("jpa.hibernate");
		if(hibernateConfiguration == null)
		{
			return null;
		}
		
		final String persistenceUnit = hibernateConfiguration.get("persistence-unit");
		
		Map<String, Object> hibernateProperties     = null;
		final Configuration propertiesConfiguration = hibernateConfiguration.child("properties");
		if(propertiesConfiguration != null)
		{
			hibernateProperties = new HashMap<>();
			hibernateProperties.putAll(propertiesConfiguration.coalescedMap());
		}
		
		final HibernateProvider hibernateProvider = HibernateProvider.New(
			persistenceUnit    ,
			hibernateProperties
		);
		
		final boolean      cache     = configuration.optBoolean("cache").orElse(true);
		final SqlConnector connector = cache
			? SqlConnector.Caching(hibernateProvider)
			: SqlConnector.New(hibernateProvider)
		;
		return SqlFileSystem.New(connector);
	}
	
}
