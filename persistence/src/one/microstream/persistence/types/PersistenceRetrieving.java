package one.microstream.persistence.types;

import java.util.function.Consumer;

public interface PersistenceRetrieving extends PersistenceObjectRetriever
{
	/* Note on naming:
	 * The main use case on the application (business logic) level is to "get" instances.
	 * Wether they are cached or have to be loaded is a technical detail from this point of view.
	 * It may even be assumed to be the general case that a desired instance is cached and the
	 * retriever instance is simply the one getting it from the cache.
	 * Hence the generic and loading-independet naming "get".
	 *
	 * The use case of an intentionally cache-ignoring concrete "load" is not deemed relevant for application design
	 * but has to be implemented via a cache-ignoring implementation of this type.
	 * Design wise, it is assumed that in modern software development, the (server) memory always holds the
	 * business-logic-validated latest and relevant state of an instance and not some outside source (like a database).
	 * So, for example, the use case "I want to get the current state of the instance from the database
	 * in case it got updated there" is not relevant/possible by design since it would be a fatally bad architecture
	 * to allow modifications that bypass the application logic (its validation etc.).
	 */

	public Object get();

	@Override
	public Object getObject(long objectId);

	public <C extends Consumer<Object>> C collect(C collector, long... objectIds);

//	public <T, C extends Collector<? super T>> C collectByType(C collector, Class<T> type);

}
