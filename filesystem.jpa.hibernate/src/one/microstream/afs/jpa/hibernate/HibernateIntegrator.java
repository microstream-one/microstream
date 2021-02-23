package one.microstream.afs.jpa.hibernate;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class HibernateIntegrator implements Integrator
{
	private static Map<String, HibernateContext> contexts = new HashMap<>();

	static HibernateContext getHibernateContext(
		final String              persistenceUnit,
		final Map<String, Object> properties
	)
	{
		Persistence.createEntityManagerFactory(persistenceUnit, properties).close();

		final HibernateContext context;
		synchronized(contexts)
		{
			context = contexts.get(persistenceUnit);
		}
		if(context == null)
		{
			throw new IllegalArgumentException("Unknown persistence unit: " + persistenceUnit);
		}
		return context;
	}

	static HibernateContext getHibernateContext(
			final String persistenceUnit
	)
	{
		return getHibernateContext(persistenceUnit, null);
	}

	public HibernateIntegrator()
	{
		super();
	}

	@Override
	public void integrate(
		final Metadata                      metadata       ,
		final SessionFactoryImplementor     sessionFactory ,
		final SessionFactoryServiceRegistry serviceRegistry
	)
	{
		final HibernateContext hibernateContext = new HibernateContext.Default(
			metadata                        ,
			sessionFactory.getJdbcServices(),
			sessionFactory.getProperties()
		);
		synchronized(contexts)
		{
			contexts.put(hibernateContext.persistenceUnit(), hibernateContext);
		}
	}

	@Override
	public void disintegrate(
		final SessionFactoryImplementor     sessionFactory ,
		final SessionFactoryServiceRegistry serviceRegistry
	)
	{
		// no-op
	}

}
