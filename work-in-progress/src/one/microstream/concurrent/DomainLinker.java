package one.microstream.concurrent;

import static one.microstream.X.notNull;

import one.microstream.collections.types.XTable;

public interface DomainLinker
{
	public <E> boolean link(Domain<E> domain, DomainLogic<? super E, ?> logic);
	
	
	
	public static DomainLinker New(final XTable<Domain<?>, DomainLogic<?, ?>> linkedLogics)
	{
		return new DomainLinker.Default(
			notNull(linkedLogics)
		);
	}
	
	// a type just to link the two Es together and then that is not supported by lambdas. Great.
	public final class Default implements DomainLinker
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XTable<Domain<?>, DomainLogic<?, ?>> linkedLogics;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final XTable<Domain<?>, DomainLogic<?, ?>> linkedLogics)
		{
			super();
			this.linkedLogics = linkedLogics;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <E> boolean link(final Domain<E> domain, final DomainLogic<? super E, ?> logic)
		{
			return this.linkedLogics.add(domain, logic);
		}
		
	}
	
}
