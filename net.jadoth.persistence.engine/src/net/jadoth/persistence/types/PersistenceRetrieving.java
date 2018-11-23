package net.jadoth.persistence.types;

import java.util.function.Consumer;

public interface PersistenceRetrieving extends PersistenceObjectSupplier
{
	/* Note on naming:
	 * The main use case on the application (business logic) level is to "get" instances.
	 * Wether they are cached or have to be loaded is a technical detail from this point of view.
	 * It may even be assumed to be the general case that a desired instance is cached and the
	 * retriever instance is simply the one getting it from the cache.
	 * Hence the generic and loading-independet naming "get".
	 *
	 * The use case of an intentional cache-ignoring concrete "load" is not deemed to be relevant for API design
	 * but has to be implemented via a cache-ignoring implementation of this type.
	 * Design wise, it is assumed that in modern software development, the (server) memory always holds the latest
	 * and relevant state of an instance and not some outside source (like a database).
	 * So e.g. the use case "I want to get the current state of the instance from the database in case it got updated
	 * there" is not relevant/possible by design.
	 */

	public Object get();

	@Override
	public Object get(long oid);

	public <C extends Consumer<Object>> C collect(C collector, long... oids);

//	public <T, C extends Collector<? super T>> C collectByType(C collector, Class<T> type);

}
