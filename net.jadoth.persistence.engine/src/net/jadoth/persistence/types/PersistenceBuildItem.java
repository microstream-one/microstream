package net.jadoth.persistence.types;


public interface PersistenceBuildItem<M>
{
	public interface Creator<M, I extends PersistenceBuildItem<M>>
	{
		public I createBuildItem(long oid);


		// introducing an unnecessary T here creates an aweful lot of unnecessary trouble in the generic builder
		// note: do not change parameter order without checking/fixing all call site logic!
		public I createBuildItem(
			long oid,
			PersistenceTypeHandler<M, Object> typeHandler,
			Object instance
		);

		public I createSkipBuildItem(long oid, Object instance);
	}

}
