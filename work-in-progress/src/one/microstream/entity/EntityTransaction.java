package one.microstream.entity;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;

import one.microstream.collections.HashTable;
import one.microstream.collections.types.XIterable;

public interface EntityTransaction extends XIterable<EntityTransaction.Entry>, TransactionContext
{
	public boolean isCommitted();
	
	public void commit();
	
	/* (29.11.2017 TM)NOTE: an "isCommittable" method is useless because
	 * before all entity locks are obtained AND held, the deciding state can change at any time.
	 * The only reliable method is to try and commit and handle exceptions if required.
	 */
//	public boolean isCommittable();
	
	public interface Entry
	{
		public Entity original();
		
		public Entity local();
		
		public boolean isCommittable();
		
		public void checkCommittable(DataConflictAcceptor dataConflictAcceptor);
	}
	
	@FunctionalInterface
	public interface DataConflictAcceptor
	{
		public void acceptConflict(
			Entity localOriginalData,
			Entity localModifiedData,
			Entity currentData
		);
	}
	
	public interface DataConflict
	{
		public Entity localOriginalData();
		
		public Entity localModifiedData();
		
		public Entity currentData();
		
		
		
		public final class Default implements DataConflict
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final Entity localOriginalData;
			private final Entity localModifiedData;
			private final Entity currentData      ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(
				final Entity localOriginalData,
				final Entity localModifiedData,
				final Entity currentData
			)
			{
				super();
				this.localOriginalData = localOriginalData;
				this.localModifiedData = localModifiedData;
				this.currentData       = currentData      ;
			}
			
			@Override
			public final Entity localModifiedData()
			{
				return this.localModifiedData;
			}
			
			@Override
			public final Entity localOriginalData()
			{
				return this.localOriginalData;
			}
			
			@Override
			public final Entity currentData()
			{
				return this.currentData;
			}
			
		}
		
	}
	

	
	public static EntityTransaction New()
	{
		return new EntityTransaction.Default();
	}
	
	public final class Default implements EntityTransaction
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final HashTable<Entity, Entry> entries     = HashTable.New();
		private       boolean                  isCommitted;
		
		private static final Comparator<Object> compareHashCode = (final Object o1, final Object o2) ->
		{
			return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
		};
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized <P extends Consumer<? super EntityTransaction.Entry>> P iterate(final P procedure)
		{
			return this.entries.values().iterate(procedure);
		}
		
		@Override
		public synchronized Entity lookupData(final Committable entity)
		{
			final Entry entry = this.entries.get(entity);
			return entry == null ? null : entry.local();
		}
		
		@Override
		public synchronized Entity ensureData(final Committable entity)
		{
			return this.ensureEntryUnsynched(entity).local();
		}
		
		@Override
		public synchronized Entity updateData(final Committable entity, final Entity newData)
		{
			// $data() is called again just to make sure that an actual data instance is set.
			/* Note:
			 * This is correct even if the passed newData instance uses a Committable layer instance that
			 * routes back here. In the end, the local data instance is looked up and used and that is exactely
			 * the desired behavior.
			 */
			return this.ensureEntryUnsynched(entity).local(Entity.data(newData));
		}
		
		private Entry ensureEntryUnsynched(final Committable entity)
		{
			Entry entry = this.entries.get(Entity.identity(entity));
			if(entry == null)
			{
				this.entries.add(entity, entry = new Entry(entity));
				
				/* (28.11.2017 TM)NOTE: potential deadlock.
				 * Recursively or iteratively, locking a lot of instances might generically cause deadlocks.
				 * To avoid that at least between two instances of this class, the entries are sorted
				 * by Entity instance system hashcode to establish a strict order on which to lock along safely.
				 * However, should two instances ever have the same hashcode, a deadlock still might occur.
				 * Still, that would only reduce the risk, not eliminate it. It would be very, very small, but not 0.
				 */
				this.entries.keys().sort(compareHashCode);
			}
			return entry;
		}

		@Override
		public synchronized boolean isCommitted()
		{
			return this.isCommitted;
		}
		
		@Override
		public synchronized void commit()
		{
			if(this.isCommitted())
			{
				throw new EntityTransactionExceptionAlreadyCommitted();
			}
			
			final HashTable<Entity, DataConflict> conflicts = HashTable.New();
			
			// quick check without lock to find stable conflicts without causing too much locking
			this.checkCommittable(conflicts);
			
			this.lockAllEntitiesAndExecute(() ->
			{
				// check again while under the protection of having locked every entity.
				this.checkCommittable(conflicts);
				
				// if the check succeeds, commit all changes
				this.entries.values().iterate(Entry::commit);
				
				// leaving this block will release all entity locks recursively
			});
			
			this.isCommitted = true;
			this.entries.clear();
		}
		
		private void lockAllEntitiesAndExecute(final Runnable logic)
		{
			/*
			 * (28.11.2017 TM)NOTE: Unsafe#monitorEnter() would be the perfect tool for this task.
			 * Or more precisely, it is the ONLY tool that allows to lock multiple instances
			 * iteratively instead of recursively.
			 * But the JKD dumb fucks (yes, I write that) removed it just because of statistical analysis
			 * instead of using their brain and recognizing its unique use independently of usage counts.
			 * The moronic java.util.concurrency Lock thingy is not a solution because the entity instance itself
			 * has to be locked to guarantee compatibility to arbitrary usage. Some external locking representation
			 * instance helps nothing for cases in which an actual instance itself has to be locked.
			 * Once again, I can only shake my head over the JDK. It has hilarously badly written code. It has
			 * tons of misconceptions, bugs, missing basic functionality (like incompetent collections,
			 * a bugged Optional type, idiotic streams, idiotic low-level IO-restriction to 2 GB just because of
			 * ONE toArray() method etc.). And now they even remove internal methods that are crucial to high-end
			 * software writing.
			 * Just because all the newbies out there don't know how to write efficient code and don't use it.
			 * Idiots. Idiots everywhere.
			 * 
			 * So the only long term viable solution is to implement the locking recursively instead of iteratively.
			 * With all its problems like an exploding stack if the entity count gets too high.
			 * Well done, JDK morons.
			 */
			
			this.recursivelyLockAndExecute(this.entries.values().iterator(), logic);
		}
		
		private void recursivelyLockAndExecute(final Iterator<Entry> iterator, final Runnable logic)
		{
			if(iterator.hasNext())
			{
				final EntityTransaction.Entry entry = iterator.next();
				
				// recursive multi-instance-lock. Better not have too many entities.
				synchronized(Entity.identity(entry.original()))
				{
					recursivelyLockAndExecute(iterator, logic);
				}
			}
			else
			{
				logic.run();
			}
		}
		
		
		private void checkCommittable(final HashTable<Entity, DataConflict> conflicts)
		{
			final DataConflictAcceptor acceptor = new DataConflictAcceptor()
			{
				@Override
				public void acceptConflict(
					final Entity localOriginalData,
					final Entity localModifiedData,
					final Entity currentData
				)
				{
					conflicts.add(
						Entity.identity(localOriginalData),
						new DataConflict.Default(
							localOriginalData,
							localModifiedData,
							currentData
						)
					);
				}
			};
			
			this.iterate(e ->
			{
				e.checkCommittable(acceptor);
			});
			
			if(!conflicts.isEmpty())
			{
				throw new EntityTransactionExceptionDataConflict(conflicts);
			}
		}

		// (28.11.2017 TM)NOTE: possible optimization: the entry could be
		static final class Entry implements EntityTransaction.Entry
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final Committable committable ;
			private final Entity      originalData;
			private       Entity      localData   ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Entry(final Committable committable)
			{
				super();
				this.localData = this.originalData = (this.committable = committable).actualData();
			}

			@Override
			public final Entity original()
			{
				return this.originalData;
			}

			@Override
			public final Entity local()
			{
				return this.localData;
			}
			
			@Override
			public final boolean isCommittable()
			{
				// must be exactely one $data() call to guarantee consistency.
				return this.originalData == this.committable.actualData();
			}
			
			@Override
			public void checkCommittable(final DataConflictAcceptor acceptor)
			{
				// must be exactely one $data() call to guarantee consistency.
				final Entity currentData = this.committable.actualData();
				
				if(this.originalData != currentData)
				{
					acceptor.acceptConflict(this.originalData, this.localData, currentData);
				}
			}
			
			final void commit()
			{
				this.committable.commit();
			}
			
			final Entity local(final Entity local)
			{
				final Entity current = this.localData;
				this.localData = local;
				return current;
			}
			
		}

	}
}
