package one.microstream.util;

import static one.microstream.X.coalesce;

import one.microstream.functional.InstanceDispatcherLogic;

public interface InstanceDispatcher
{
	public InstanceDispatcher setInstanceDispatcherLogic(InstanceDispatcherLogic logic);
	
	public InstanceDispatcherLogic getInstanceDispatcherLogic();
	
	
	
	public class Default implements InstanceDispatcher
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final InstanceDispatcherLogic NO_OP = new InstanceDispatcherLogic()
		{
			@Override
			public <T> T apply(final T subject)
			{
				return subject;
			}
		};
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private InstanceDispatcherLogic logic = NO_OP;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////
		
		protected final <T> T dispatch(final T newInstance)
		{
			return this.logic.apply(newInstance);
		}
		
		@Override
		public InstanceDispatcher setInstanceDispatcherLogic(final InstanceDispatcherLogic instanceDispatcher)
		{
			this.logic = coalesce(instanceDispatcher, NO_OP);
			return this;
		}
		
		@Override
		public InstanceDispatcherLogic getInstanceDispatcherLogic()
		{
			return this.logic;
		}
		
	}

}
