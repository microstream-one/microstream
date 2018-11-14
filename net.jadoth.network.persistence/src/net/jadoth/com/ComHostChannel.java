package net.jadoth.com;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceManager;

public interface ComHostChannel extends ComChannel
{
	// empty so far, but who knows
	
	public static ComHostChannel New(final PersistenceManager<?> persistenceManager)
	{
		return new ComHostChannel.Implementation(
			notNull(persistenceManager)
		);
	}
	
	public final class Implementation extends ComChannel.Implementation implements ComHostChannel
	{
		Implementation(final PersistenceManager<?> persistenceManager)
		{
			super(persistenceManager);
		}
	}
	
}
