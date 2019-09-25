package one.microstream.concurrent;

import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XTable;

public interface DomainTaskCreator
{
	public void createDomainTasks(
		XGettingTable<Domain<?>, DomainLogic<?, ?>> linkedLogics,
		XTable<Domain<?>, DomainTask>               createdTasks
	);
}
