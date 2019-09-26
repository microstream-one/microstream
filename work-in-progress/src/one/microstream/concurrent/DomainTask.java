package one.microstream.concurrent;

import static one.microstream.X.notNull;

public interface DomainTask<E, R>
{
	public R executeOn(E domainEntityRoot);
	
	
	public static <E, R> DomainTask<E, R> New(final DomainLogic<? super E, R> linkedLogic)
	{
		return new DomainTask.Default<>(
			notNull(linkedLogic)
		);
	}
	
	public final class Default<E, R> implements DomainTask<E, R>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final DomainLogic<? super E, R> linkedLogic;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final DomainLogic<? super E, R> linkedLogic)
		{
			super();
			this.linkedLogic = linkedLogic;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public R executeOn(final E domainEntityRoot)
		{
			/* (26.09.2019 TM)FIXME: Concurrency: Parent Task communication
			 * Must report the result back to the parent ApplicationTask instance (includint parent.notify())
			 * Also:
			 * Must have the option to execute again with the previous result if the parent task's completen
			 * demands it.
			 * Actually, not "again", but another logic. Or more precisely: a sequence of DomainLogics
			 * instead of just one, with everyone getting the result of the previous one as an additional input
			 * value, where the first one gets null as that addtional input, of course.
			 * However, the "previous round" additional input must come from the parent application task, not from
			 * this DomainTask (domain-specific sub-task), since otherwise, the two logics could have combined
			 * into a single one right away, in the first place.
			 * Complicated ...
			 */
			return this.linkedLogic.executeDomainLogic(domainEntityRoot);
		}
		
	}
	
}
