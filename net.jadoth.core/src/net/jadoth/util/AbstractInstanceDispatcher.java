package net.jadoth.util;

import static net.jadoth.X.coalesce;

import net.jadoth.functional.Dispatcher;

public abstract class AbstractInstanceDispatcher
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final Dispatcher NO_OP = new Dispatcher()
	{
		@Override
		public <T> T apply(final T subject)
		{
			return subject;
		}
	};



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private Dispatcher dispatcher = NO_OP;



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected <T> T dispatch(final T newInstance)
	{
		return this.dispatcher.apply(newInstance);
	}

	protected void internalSetDispatcher(final Dispatcher instanceDispatcher)
	{
		this.dispatcher = coalesce(instanceDispatcher, NO_OP);
	}

	protected Dispatcher internalGetDispatcher()
	{
		return this.dispatcher;
	}

}
