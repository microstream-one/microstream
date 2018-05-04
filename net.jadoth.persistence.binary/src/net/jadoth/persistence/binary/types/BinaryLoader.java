package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.chars.XStrings;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.types.PersistenceDistrict;
import net.jadoth.persistence.types.PersistenceLoader;
import net.jadoth.persistence.types.PersistenceRoots;
import net.jadoth.persistence.types.PersistenceSource;
import net.jadoth.persistence.types.PersistenceSwizzleSupplier;
import net.jadoth.reference._intReference;
import net.jadoth.swizzling.types.SwizzleObjectSupplier;
import net.jadoth.typing.KeyValue;

public interface BinaryLoader extends PersistenceLoader<Binary>, BinaryBuilder
{
	public interface Creator extends PersistenceLoader.Creator<Binary>
	{
		@Override
		public BinaryLoader createBuilder(
			PersistenceDistrict<Binary>        district,
			PersistenceSwizzleSupplier<Binary> source
		);
	}



	public final class Implementation extends BinaryBuilder.AbstractImplementation implements BinaryLoader
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceSwizzleSupplier<Binary> swizzleSupplier;
		private final LoadItemsChain                     loadItems      ;
		private final HashTable<Object, Object>          helpers        ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		/* (17.12.2012)TODO: thread local swizzle registry
		 * for fast thread local graph building
		 * (optional) commit to parent global swizzle registry
		 */
		public Implementation(
			final PersistenceSwizzleSupplier<Binary> source   ,
			final PersistenceDistrict<Binary>        district ,
			final LoadItemsChain                     loadItems
		)
		{
			super(district);
			this.swizzleSupplier = notNull(source)   ;
			this.loadItems       = notNull(loadItems);
			this.helpers         = HashTable.New();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void readLoadOnce()
		{
			this.addChunks(this.swizzleSupplier.source().readInitial());

			/* the processing of the initial read might have resulted in reference oids that have to be loaded
			 * (e.g. the initial read returns a root instance). So call the standard loading method at this point.
			 * If no additional load items have been added, the method will return very quickly.
			 */
			this.readLoadOidData();
		}

		private void readLoadOidData()
		{
			/* (09.09.2015 TM)TODO: persistence loading consistency
			 * Doesn't the incremental loading of references have to be an atomic process on the source side
			 * (e.g. storage engine) to avoid potentical inconstencies by concurrent write requests?
			 * As BinaryLoading and Storage are decoupled, maybe both should be implemented:
			 * The storage task collects all required items via reference iteration by the handler and
			 * returns the completed result to the loader.
			 * This logic here will then never need to do more than 1 loop, but CAN do more loops if the source
			 * only provides exactely what is requested (e.g. a simple file source with no concurrency issues).
			 *
			 * On problem is, however: the storage task processing may not return instances that are already loaded
			 * and registered in the registry. Otherwise, every load request could potentially load "half the database",
			 * just to discard most of the data again while building.
			 * So lookup-access to the registry would have to be provided to the storage task as well.
			 *
			 * Concept:
			 * The Storage load request task gets passed a function of the form
			 * boolean requireOid(long)
			 * that is called for every OID to be loaded by the storage entity type handler while deep-loading
			 * references.
			 * The function is or delegates to this instance, which in turn asks the swizzle registry, if the instance
			 * is already present. If yes, it is enqueued as a build item (to ensure a strong reference) and for later
			 * building. If no, then a new blank instance of the type and length is enqueued as a local build item.
			 * Return values are no/yes accordingly.
			 *
			 * Problems:
			 * 1.) The storage (entity type handler) needs a way to recognize the special nature of Lazy
			 * references. Otherwise, the lazy reference would become effectively eager, i.e. stop working.
			 * In the end, this means to enhance the type description syntax with a "do not deep load references"
			 * marker.
			 * Which is kind of ugly :(.
			 * The currently used logic-side BinaryHandler solution is much more elegant...
			 *
			 * 2.) maybe, once all the data is assembled, the loader has to acquire and keep a lock on the swizzle
			 * registry for the whole building process. Otherwise, it could occur that concurrent modifications in the
			 * the swizzle registry lead to a mixture of entity state (inconcistencies).
			 * Or maybe than can never happen as the memory is always more current than the database and building
			 * the graph automatically creates the right state. Tricky ... needs more thought.
			 *
			 *
			 * HM...
			 *
			 * OR is it maybe not required at all because:
			 * - the application memory always has the most current version of an entity
			 * - if an entity already exists in the registry, it will never be required to be loaded at all
			 * - therefore, a concurrent write to the DB CANNOT create an inconsistent state.
			 *   The situation "Thread A load part 1, Thread B write entity X, Thread A load part 2 includint entity X"
			 *   can never occur.
			 * Is that correct?
			 */

			final PersistenceSource<Binary> source = this.swizzleSupplier.source();
			while(!this.loadItems.isEmpty())
			{
				this.addChunks(source.readByObjectIds(this.loadItems.getObjectIdSets()));
			}
		}

		@Override
		protected void addChunks(final XGettingCollection<? extends Binary> chunks)
		{
			this.loadItems.clear();
			super.addChunks(chunks);
		}

		private void populate(final Consumer<Object> collector, final long... oids)
		{
			for(int i = 0; i < oids.length; i++)
			{
				collector.accept(this.getBuildInstance(oids[i]));
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		// synchronized to force byte code execution order (prevent chunk collection) and for just-in-case thread-safety
		@Override
		public final synchronized Object initialGet()
		{
			this.readLoadOnce();
			this.build();
			this.commit();
			final Object instance = this.internalGetFirst();
			this.clearBuildItems();
			// JadothConsole.debugln("Returning instance.");
			return instance;
		}

		// synchronized to force byte code execution order (prevent chunk collection) and for just-in-case thread-safety
		@Override
		public final synchronized Object get(final long oid)
		{
			this.requireReference(oid);
			this.readLoadOidData();
			this.build();
			this.commit();
			final Object instance = this.getBuildInstance(oid);
			this.clearBuildItems();
			return instance;
		}

		// synchronized to force byte code execution order (prevent chunk collection) and for just-in-case thread-safety
		@Override
		public final synchronized <C extends Consumer<Object>> C collect(final C collector, final long... oids)
		{
			for(int i = 0; i < oids.length; i++)
			{
				this.requireReference(oids[i]);
			}
			this.readLoadOidData();
			this.build();
			this.commit();
			this.populate(collector, oids);
			this.clearBuildItems();
			return collector;
		}

		@Override
		public PersistenceRoots loadRoots()
		{
			final Object initial = this.initialGet();

			if(initial == null)
			{
				return null; // might be null if there is no data at all
			}

			/*
			 * Not sure if this instanceof is a hack or clean. The intention / rationale is:
			 * A datasource that supports / contains persistence roots (e.g. graph database or a file writer
			 * using the concept) must return that instance on the initial get anyway.
			 * For a datasource that does not support it, this method would have to throw an exception anyway,
			 * one way or another.
			 * So design-wise, it should be viable to just relay to the initialGet() and make a runtime type check.
			 */
			if(!(initial instanceof PersistenceRoots))
			{
				// (21.10.2013 TM)EXCP: proper exception
				throw new RuntimeException("Initially read data is no roots instance");
			}
			return (PersistenceRoots)initial;
		}

		@Override
		public final void registerSkip(final long oid)
		{
			this.registerSkipOid(oid);
		}

		@Override
		protected final void requireReference(final long refOid)
		{
			// add-logic: only put if not contained yet (single-lookup)
			this.loadItems.addLoadItem(refOid);
		}

		@Override
		public SwizzleObjectSupplier getSwizzleObjectSupplier()
		{
			return this.swizzleSupplier;
		}

		@Override
		public boolean registerHelper(final Object subject, final Object helper)
		{
			synchronized(this.helpers)
			{
				final KeyValue<Object, Object> existingEntry = this.helpers.addGet(subject, helper);
				if(existingEntry == null)
				{
					return true;
				}

				if(existingEntry.value() == helper)
				{
					return false;
				}

				// (21.04.2016 TM)EXCP: proper exception
				throw new RuntimeException(
					"Conflicting helper registration: "
					+ XStrings.systemString(subject) + " already has helper instance "
					+ XStrings.systemString(existingEntry.value()) + " associated with it. Not "
					+ XStrings.systemString(helper)
				);
			}
		}

		@Override
		public Object getHelper(final Object subject)
		{
			synchronized(this.helpers)
			{
				return this.helpers.get(subject);
			}
		}

		@Override
		public Object removeHelper(final Object subject)
		{
			synchronized(this.helpers)
			{
				return this.helpers.removeFor(subject);
			}
		}

	}



	public final class CreatorSimple implements BinaryLoader.Creator
	{
		@Override
		public BinaryLoader createBuilder(
			final PersistenceDistrict<Binary>        district,
			final PersistenceSwizzleSupplier<Binary> source
		)
		{
			return new BinaryLoader.Implementation(source, district, new LoadItemsChain.Simple());
		}

	}



	public final class CreatorChannelHashing implements BinaryLoader.Creator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final _intReference hashSizeProvider;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public CreatorChannelHashing(final _intReference hashSizeProvider)
		{
			super();
			this.hashSizeProvider = hashSizeProvider;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public BinaryLoader createBuilder(
			final PersistenceDistrict<Binary>        district,
			final PersistenceSwizzleSupplier<Binary> source
		)
		{
			return new BinaryLoader.Implementation(
				source,
				district,
				new LoadItemsChain.ChannelHashing(this.hashSizeProvider.get())
			);
		}

	}

}
