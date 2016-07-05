package net.jadoth.persistence.types;

import net.jadoth.swizzling.types.SwizzleBuildLinker;

public interface PersistenceBuilder<M> extends SwizzleBuildLinker
{
	// (27.04.2013)NOTE: why are those implementation detail methods in the public interface?
//	public void addChunks(XGettingCollection<? extends M> chunks);
//
//	public Object build();
//
//	public void commit();



	public interface Creator<M>
	{
		public PersistenceBuilder<M> createPersistenceBuilder();
	}

}
