package one.microstream.afs.jpa.hibernate;

import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;

public interface HibernateContext
{
	public Metadata metadata();

	public JdbcServices jdbcServices();

	public default Dialect dialect()
	{
		return this.jdbcServices().getDialect();
	}

	public Map<String, Object> properties();

	public default String persistenceUnit()
	{
		return (String)this.properties().get("hibernate.ejb.persistenceUnitName");
	}

	public default String defaultCatalog()
	{
		return (String)this.properties().get("hibernate.default_catalog");
	}

	public default String defaultSchema()
	{
		return (String)this.properties().get("hibernate.default_schema");
	}



	public static class Default implements HibernateContext
	{
		private final Metadata              metadata    ;
		private final JdbcServices          jdbcServices;
		private final Map<String, Object>   properties  ;

		Default(
			final Metadata            metadata    ,
			final JdbcServices        jdbcServices,
			final Map<String, Object> properties
		)
		{
			super();
			this.metadata     = metadata    ;
			this.jdbcServices = jdbcServices;
			this.properties   = properties  ;
		}

		@Override
		public Metadata metadata()
		{
			return this.metadata;
		}

		@Override
		public JdbcServices jdbcServices()
		{
			return this.jdbcServices;
		}

		@Override
		public Map<String, Object> properties()
		{
			return this.properties;
		}

	}

}
