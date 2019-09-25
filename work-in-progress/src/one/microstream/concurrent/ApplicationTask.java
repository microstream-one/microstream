package one.microstream.concurrent;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XTable;

public interface ApplicationTask<A>
{
	public void linkDomains(A applicationRoot, XTable<Domain<?>, DomainLogic<?, ?>> linkedLogics);
	
	public final class Default<A> implements ApplicationTask<A>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// (25.09.2019 TM)TODO: Concurrency: Just "A" or "? extends A" or "? super A"?
		private final XGettingCollection<? extends ApplicationTaskItem<A>> taskitems;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final XGettingCollection<? extends ApplicationTaskItem<A>> taskitems)
		{
			super();
			this.taskitems = taskitems;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void linkDomains(final A applicationRoot, final XTable<Domain<?>, DomainLogic<?, ?>> linkedLogics)
		{
			// a type just to link the two Es together and then that is not supported by lambdas. Great.
			final DomainLinker linker = DomainLinker.New(linkedLogics);
			
			for(final ApplicationTaskItem<A> item : this.taskitems)
			{
				item.link(applicationRoot, linker);
			}
		}
		
	}
	
}
