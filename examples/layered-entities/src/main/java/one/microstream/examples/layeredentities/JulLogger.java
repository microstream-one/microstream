package one.microstream.examples.layeredentities;

import java.util.logging.Logger;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLogger;

public class JulLogger implements EntityLogger
{
	public JulLogger()
	{
		super();
	}
	
	@Override
	public void entityCreated(final Entity identity, final Entity data)
	{
		Logger.getLogger(identity.getClass().getName())
		.info("Entity created: " +  data);
	}
	
	@Override
	public void afterUpdate(
		final Entity identity, 
		final Entity data, 
		final boolean successful
	)
	{
		Logger.getLogger(identity.getClass().getName())
			.info("Entity updated: " + data);
	}
}
