package net.jadoth.network.persistence;

import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.persistence.types.PersistenceTarget;

public interface NetworkPersistenceConnection<M> extends PersistenceTarget<M>, PersistenceSource<M>
{

}
