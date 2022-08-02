package one.microstream.persistence.types;

import one.microstream.collections.Set_long;
import one.microstream.functional._longPredicate;

public interface ObjectIdsProcessor
{
	// one-by-one processing of objectIds. Efficient for embedded mode, horribly inefficient for server mode.
	public void processObjectIdsByFilter(_longPredicate objectIdsSelector);

	// for bulk processing of objectIds. Most efficient way for server mode, inefficient for embedded mode.
	public Set_long provideObjectIdsBaseSet();
}
