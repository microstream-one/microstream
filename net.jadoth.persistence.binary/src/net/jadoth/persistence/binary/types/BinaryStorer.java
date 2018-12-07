package net.jadoth.persistence.binary.types;

import static java.lang.System.identityHashCode;
import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceEagerStoringFieldEvaluator;
import net.jadoth.persistence.types.PersistenceHandler;
import net.jadoth.persistence.types.PersistenceObjectManager;
import net.jadoth.persistence.types.PersistenceObjectRetriever;
import net.jadoth.persistence.types.PersistenceStorer;
import net.jadoth.persistence.types.PersistenceTarget;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.reference._intReference;
import net.jadoth.util.BufferSizeProviderIncremental;


public interface BinaryStorer extends PersistenceStorer<Binary>
{
	@Override
	public BinaryStorer initialize();

	@Override
	public BinaryStorer initialize(long initialCapacity);

	@Override
	public PersistenceStorer<Binary> reinitialize();

	@Override
	public PersistenceStorer<Binary> reinitialize(long initialCapacity);

	@Override
	public PersistenceStorer<Binary> ensureCapacity(long desiredCapacity);

	@Override
	public long currentCapacity();

	@Override
	public long maximumCapacity();


	
	/**
	 * Default implementation that stores referenced instances only if required (i.e. if they have no OID assigned yet,
	 * therefore have not been stored yet, therefore require to be stored). It can be seen as a "lazy" or "on demand"
	 * storer as opposed to{@link ImplementationEager}.<br>
	 * For a more differentiated solution between the two simple, but extreme strategies,
	 * see {@link PersistenceEagerStoringFieldEvaluator}.
	 * 
	 * @author TM
	 */
	public class Implementation implements BinaryStorer, PersistenceHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		protected static int defaultSlotSize()
		{
			// why permanently occupy additional memory with fields and instances for constant values?
			return 1024; // anthing below 1024 doesn't pay of
		}

		protected static final int maximumSlotSize()
		{
			return 1_073_741_824; // technical 2^n int limit
		}

		protected static final int padCapacity(final long n)
		{
			if(n >= maximumSlotSize())
			{
				return maximumSlotSize();
			}
			int i = 1;
			while(i < n)
			{
				i <<= 1;
			}
			return i;
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// (12.04.2013)XXX: encapsulate by "PersistenceContextManager" complementary to builder?
		private final PersistenceObjectManager              objectManager  ;
		private final PersistenceObjectRetriever            objectRetriever;
		private final PersistenceTypeHandlerManager<Binary> typeManager    ;
		private final PersistenceTarget<Binary>             target         ;
		
		// channel hashing fields
		private final BufferSizeProviderIncremental bufferSizeProvider;
		private final int                           chunksHashRange   ;
		private final ChunksBuffer[]                chunks            ;

		/*
		 * item hashing structures get initialized lazily for the following reasons:
		 * - the storer instance can commit (be cleared) and be reinitialized multiple times.
		 * - the storer instance can be explicitly initialized to a certain capacity.
		 * - clearing after committing can simply null the array reference, easing garbage collection.
		 */
		final   Item   head = new Item(null, 0L, null, null);
		private Item   tail = this.head;
		private Item[] hashSlots;
		private int    hashRange;
		private long   itemCount;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final PersistenceObjectManager              objectManager     ,
			final PersistenceObjectRetriever            objectSupplier    ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   chunkHashSize
		)
		{
			super();
			this.objectManager      = notNull(objectManager)         ;
			this.objectRetriever    = notNull(objectSupplier)        ;
			this.typeManager        = notNull(typeManager)           ;
			this.target             = notNull(target)                ;
			this.bufferSizeProvider = notNull(bufferSizeProvider)    ;
			this.chunks             = new ChunksBuffer[chunkHashSize];
			this.chunksHashRange    = chunkHashSize - 1              ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected ChunksBuffer chunk(final long oid)
		{
			return this.chunks[(int)(oid & this.chunksHashRange)];
		}

		protected Binary[] complete()
		{
			// (21.03.2016 TM)NOTE: added to avoid NPEs
			this.initialize();

			for(final ChunksBuffer chunk : this.chunks)
			{
				chunk.complete();
			}

			/*
			 * Must return a local copy of the array as the task may not use this instance's interal array.
			 * For example:
			 * - storer clears the array after completing, while task needs an uncleared array to clear
			 *   the chunks themselves
			 * - task implementation might mutate the array, potentially ruining the storer's state
			 * The chunks array is usually tiny (the length is equal to the channel count)
			 */
			return this.chunks.clone();
		}

		@Override
		public <T> long apply(final T instance)
		{
			if(instance == null)
			{
				return Persistence.nullId();
			}

			final long oidLocal;
			if((oidLocal = this.lookupOid(instance)) != Persistence.nullId())
			{
				return oidLocal;
			}

			/* (07.12.2018 TM)TODO: JET-48 solution
			 * maybe decouple objectManager here into objectRegistry and objectIdProvider,
			 * then then lookup and id ensuring must happen under the SAME lock, otherwise concurrency bug.
			 * The contextDispatcher could then replace the objectRegistry and objectIdProvider by local instaces.
			 * alternatively, a while objectmanager wrapper could be used here without change.
			 */
			final long oidGlobal;
			if((oidGlobal = this.objectManager.lookupObjectId(instance)) != Persistence.nullId())
			{
				// (07.12.2018 TM)FIXME: explain eager/lazy here
				return oidGlobal;
			}

			return this.registerAdd(instance);
		}
		
		@Override
		public final <T> long applyEager(final T instance)
		{
			if(instance == null)
			{
				return Persistence.nullId();
			}

			// (07.12.2018 TM)FIXME: update comment
			/*
			 * "Eager" must still meansthat if this storer has already stored the passed instance,
			 * it may not store it again. That would not only be data-wise redundant and unnecessary,
			 * but would also create infinite storing loops and overflows.
			 * So "eager" can only mean to not check the global registry, but it must still mean to check
			 * the local registry.
			 */
			final long oidLocal;
			if((oidLocal = this.lookupOid(instance)) != Persistence.nullId())
			{
				return oidLocal;
			}

			return this.registerAdd(instance);
		}

		@Override
		public final PersistenceObjectRetriever getObjectSupplier()
		{
			return this.objectRetriever;
		}

		@Override
		public BinaryStorer initialize()
		{
			this.ensureInitialized();
			return this;
		}

		@Override
		public BinaryStorer initialize(final long initialCapacity)
		{
			if(this.isInitialized())
			{
				return this;
			}
			this.internalInitialize(padCapacity(initialCapacity));
			return this;
		}

		@Override
		public PersistenceStorer<Binary> reinitialize()
		{
			this.clear();
			this.internalInitialize();
			return this;
		}

		@Override
		public PersistenceStorer<Binary> reinitialize(final long initialCapacity)
		{
			this.clear();
			this.internalInitialize(padCapacity(initialCapacity));
			return this;
		}

		private void internalInitialize()
		{
			this.internalInitialize(defaultSlotSize());
		}

		protected void internalInitialize(final int pow2Capacity)
		{
			this.hashSlots = new Item[pow2Capacity];
			this.hashRange = pow2Capacity - 1;
			
			final ChunksBuffer[] chunks = this.chunks;
			for(int i = 0; i < chunks.length; i++)
			{
				chunks[i] = ChunksBuffer.New(this.bufferSizeProvider);
			}
		}

		@Override
		public final long currentCapacity()
		{
			return this.hashSlots == null
				? 0
				: this.hashSlots.length
			;
		}

		@Override
		public final long maximumCapacity()
		{
			return maximumSlotSize();
		}

		@Override
		public PersistenceStorer<Binary> ensureCapacity(final long desiredCapacity)
		{
			if(this.currentCapacity() >= desiredCapacity)
			{
				return this;
			}
			this.rebuildStoreItems(padCapacity(desiredCapacity));
			return this;
		}

		private void ensureInitialized()
		{
			if(this.isInitialized())
			{
				return;
			}
			this.internalInitialize();
		}

		@Override
		public final boolean isInitialized()
		{
			return this.hashSlots != null;
		}

		@Override
		public final long size()
		{
			return this.itemCount;
		}

		@Override
		public final long store(final Object root)
		{
			this.ensureInitialized();
			return this.storeGraph(root);
		}

		@Override
		public final long[] storeAll(final Object... instances)
		{
			this.ensureInitialized();

			final long[] oids = new long[instances.length];
			for(int i = 0; i < instances.length; i++)
			{
				oids[i] = this.storeGraph(instances[i]);
			}
			return oids;
		}
		
		@Override
		public void storeAll(final Iterable<?> instances)
		{
			this.ensureInitialized();

			for(final Object instance : instances)
			{
				this.storeGraph(instance);
			}
		}

		@Override
		public final Object commit()
		{
			if(!this.isEmpty())
			{
				this.target.write(this.complete());
			}
			this.clear();
			return null;
		}

		@Override
		public void clear()
		{
			this.clearRegistered();
			this.clearChunks();
		}
		
		protected void clearChunks()
		{
			final ChunksBuffer[] chunks = this.chunks;
			for(int i = 0; i < chunks.length; i++)
			{
				/* Note:
				 * may explicitly NOT clear (deallocate) the current chunks
				 * because in use with embedded (in-process) storage the chunks
				 * might still be used by the storage worker threads to update their entity caches.
				 * The released chunks must be handled by those threads if existing
				 * or ultimately by the garbage collector (or by some tailored additional logic)
				 */
				chunks[i] = null;
			}
		}

		@Override
		public final void clearRegistered()
		{
			// clear hash table
			this.hashSlots = null;
			this.itemCount = 0;

			// clear item chain
			(this.tail = this.head).next = null;
		}

		public final long lookupOid(final Object instance)
		{
			for(Item e = this.hashSlots[identityHashCode(instance) & this.hashRange]; e != null; e = e.link)
			{
				if(e.instance == instance)
				{
					return e.oid;
				}
			}
			
			return Persistence.nullId();
		}

		

		public final void rebuildStoreItems()
		{
			this.rebuildStoreItems(this.hashSlots.length * 2);
		}

		public final void rebuildStoreItems(final int newLength)
		{
			// moreless academic check for more than 1 billion entries
			if(this.hashSlots.length >= maximumSlotSize())
			{
				return; // note that aborting rebuild does not ruin anything, only performance degrades
			}

			final int newRange;
			final Item[] newSlots = new Item[(newRange = newLength - 1) + 1];
			for(Item entry : this.hashSlots)
			{
				for(Item next; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[identityHashCode(entry.instance) & newRange];
					newSlots[identityHashCode(entry.instance) & newRange] = entry;
				}
			}
			this.hashSlots = newSlots;
			this.hashRange = newRange;
		}

		
		/**
		 * Stores the passed instance (always) and interprets it as the root of a graph to be traversed and
		 * have its instances stored recursively if deemed necessary by the logic until all instance
		 * that can be reached by that logic have been handled.
		 */
		protected final long storeGraph(final Object root)
		{
			final long rootOid = this.registerAdd(notNull(root));

			// process and collect required instances uniquely in item chain (graph recursion transformed to iteration)
			for(Item item = this.tail; item != null; item = item.next)
			{
				this.storeItem(item);
			}

			return rootOid;
		}
		
		protected final void storeItem(final Item item)
		{
//			XDebug.debugln("Storing\t" + item.oid + "\t" + item.typeHandler.typeName());
			item.typeHandler.store(this.chunk(item.oid), item.instance, item.oid, this);
		}
		
		protected final long registerAdd(final Object instance)
		{
			final long oid;

			// ensure handler (or fail if type is not persistable) before ensuring an OID.
			this.tail = this.tail.next = this.registerOid(
				instance,
				this.typeManager.ensureTypeHandler(instance),
				oid = this.objectManager.ensureObjectId(instance)
			);

			return oid;
		}
		
		public final Item registerOid(
			final Object                                 instance   ,
			final PersistenceTypeHandler<Binary, Object> typeHandler,
			final long                                   oid
		)
		{
			if(++this.itemCount >= this.hashRange)
			{
				this.rebuildStoreItems();
			}

			return this.hashSlots[identityHashCode(instance) & this.hashRange] =
				new Item(instance, oid, typeHandler, this.hashSlots[identityHashCode(instance) & this.hashRange])
			;
		}
		
		@Override
		public final void registerSkip(final Object instance, final long oid)
		{
			this.registerOid(instance, null, oid);
		}

		@Override
		public final void registerSkip(final Object instance)
		{
			/* (07.12.2018 TM)FIXME: why only lookup? What if it's not registered, yet?
			 * The whole skipping thing must be overhauled:
			 * - why register the oid? as a kind of lazy special case storing? Must be commented, then.
			 * - if handler == null indicates a skip-entry, then handler must be checked for null.
			 *   alternative: dummy handler that does nothing.
			 */
			this.registerOid(instance, null, this.objectManager.lookupObjectId(instance));
		}
		
	}
	
	/**
	 * Identical to {@link Implementation}, but stores every referenced instance eagerly.<br>
	 * For a more differentiated solution between the two simple, but extreme strategies,
	 * see {@link PersistenceEagerStoringFieldEvaluator}.<br>
	 * 
	 * @author TM
	 */
	public final class ImplementationEager extends Implementation
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		ImplementationEager(
			final PersistenceObjectManager              objectManager     ,
			final PersistenceObjectRetriever            objectSupplier    ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount
		)
		{
			super(objectManager, objectSupplier, typeManager, target, bufferSizeProvider, channelCount);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final <T> long apply(final T instance)
		{
			// for a "full" graph storing strategy, the logic is simply to store everything forced.
			return this.applyEager(instance);
		}
		
	}

	static final class Item
	{
		final PersistenceTypeHandler<Binary, Object> typeHandler;
		final Object                                 instance   ;
		final long                                   oid        ;
		      Item                                   link, next ;

		Item(
			final Object                                 instance   ,
			final long                                   oid        ,
			final PersistenceTypeHandler<Binary, Object> typeHandler,
			final Item                                   link
		)
		{
			super();
			this.instance    = instance   ;
			this.oid         = oid        ;
			this.typeHandler = typeHandler;
			this.link        = link       ;
		}

	}
		
	public static BinaryStorer.Creator Creator(final _intReference chunkHashSizeProvider)
	{
		return new BinaryStorer.Creator.Implementation(
			notNull(chunkHashSizeProvider)
		);
	}
		
	public interface Creator extends PersistenceStorer.Creator<Binary>
	{
		@Override
		public BinaryStorer createLazyStorer(
			PersistenceObjectManager              objectManager     ,
			PersistenceObjectRetriever            objectSupplier    ,
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProviderIncremental         bufferSizeProvider
		);
		
		@Override
		public default BinaryStorer createStorer(
			final PersistenceObjectManager              objectManager     ,
			final PersistenceObjectRetriever            objectSupplier    ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider
		)
		{
			return this.createLazyStorer(objectManager, objectSupplier, typeManager, target, bufferSizeProvider);
		}
		
		@Override
		public BinaryStorer createEagerStorer(
			PersistenceObjectManager              objectManager     ,
			PersistenceObjectRetriever            objectSupplier    ,
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProviderIncremental         bufferSizeProvider
		);
		
		
		
		public abstract class AbstractImplementation implements BinaryStorer.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields  //
			/////////////////////

			private final _intReference chunkHashSizeProvider;



			///////////////////////////////////////////////////////////////////////////
			// constructors     //
			/////////////////////

			protected AbstractImplementation(final _intReference chunkHashSizeProvider)
			{
				super();
				this.chunkHashSizeProvider = chunkHashSizeProvider;
			}

			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			protected int getChunkHashSize()
			{
				return this.chunkHashSizeProvider.get();
			}

		}
		
		public final class Implementation extends AbstractImplementation
		{
			Implementation(final _intReference chunkHashSizeProvider)
			{
				super(chunkHashSizeProvider);
			}

			@Override
			public final BinaryStorer createLazyStorer(
				final PersistenceObjectManager              objectManager     ,
				final PersistenceObjectRetriever             objectSupplier    ,
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider
			)
			{
				return new BinaryStorer.Implementation(
					objectManager          ,
					objectSupplier         ,
					typeManager            ,
					target                 ,
					bufferSizeProvider     ,
					this.getChunkHashSize()
				);
			}
			@Override
			public BinaryStorer createEagerStorer(
				final PersistenceObjectManager              objectManager     ,
				final PersistenceObjectRetriever             objectSupplier    ,
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider
			)
			{
				return new BinaryStorer.ImplementationEager(
					objectManager          ,
					objectSupplier         ,
					typeManager            ,
					target                 ,
					bufferSizeProvider     ,
					this.getChunkHashSize()
				);
			}

		}
				
	}

}
