
package one.microstream.entity.logging;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerProvider;
import one.microstream.entity.EntityLayerProviderProvider;


public interface EntityLogger extends EntityLayerProviderProvider
{
	public default void afterRead(final Entity identity, final Entity data)
	{
		// empty by default
	}
	
	public default void beforeUpdate(final Entity identity, final Entity data)
	{
		// empty by default
	}
	
	public default void afterUpdate(final Entity identity, final Entity data, final boolean successful)
	{
		// empty by default
	}
	
	@Override
	public default EntityLayerProvider provideEntityLayerProvider()
	{
		return e -> new EntityLayerLogging(e, this);
	}
}
