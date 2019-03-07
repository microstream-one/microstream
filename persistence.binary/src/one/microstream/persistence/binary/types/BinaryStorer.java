package one.microstream.persistence.binary.types;

import static java.lang.System.identityHashCode;
import static one.microstream.X.notNull;

import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceAcceptor;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceObjectRetriever;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference._intReference;
import one.microstream.util.BufferSizeProviderIncremental;


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
	public class Implementation implements BinaryStorer, PersistenceStoreHandler, PersistenceAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		protected static int defaultSlotSize()
		{
			// why permanently occupy additional memory with fields and instances for constant values?
			return 1024; // anthing below 1024 doesn't pay of
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final boolean                               switchByteOrder;
		private final PersistenceObjectManager              objectManager  ;
		private final PersistenceObjectRetriever            objectRetriever;
		private final PersistenceTypeHandlerManager<Binary> typeManager    ;
		private final PersistenceTarget<Binary>             target         ;
		
		// channel hashing fields
		private final BufferSizeProviderIncremental bufferSizeProvider;
		private final int                           chunksHashRange   ;
		
		// cannot be final since every commit needs to pass an independant instance, anyway
		private ChunksBuffer[] chunks;

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
			final PersistenceObjectRetriever            objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder
		)
		{
			super();
			this.objectManager      = notNull(objectManager)     ;
			this.objectRetriever    = notNull(objectRetriever)   ;
			this.typeManager        = notNull(typeManager)       ;
			this.target             = notNull(target)            ;
			this.bufferSizeProvider = notNull(bufferSizeProvider);
			this.chunksHashRange    =         channelCount - 1   ;
			this.switchByteOrder    =         switchByteOrder    ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected ChunksBuffer chunk(final long oid)
		{
			return this.chunks[(int)(oid & this.chunksHashRange)];
		}

		protected Binary complete()
		{
			// (21.03.2016 TM)NOTE: added to avoid NPEs
			this.ensureInitialized();

			for(final ChunksBuffer chunk : this.chunks)
			{
				chunk.complete();
			}

			// all chunks know the array internally, so passing one means passing all. And there is always at least one.
			return this.chunks[0];
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
			
			/*
			 * Lazy storing logic:
			 * If the instance already has an OID registered with it (= already known / handled globally),
			 * it is assumed to be already stored and therefore not stored here ("again").
			 * Only if a new OID has to be assigned, the instance is registered (via registerAdd)
			 */
			return this.objectManager.ensureObjectId(instance, this);
		}
		
		@Override
		public final <T> long applyEager(final T instance)
		{
			if(instance == null)
			{
				return Persistence.nullId();
			}
			
			/*
			 * "Eager" must still mean that if this storer has already stored the passed instance,
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

			/*
			 * Eager storing logic:
			 * If the instance is not already handled locally (already stored by this storer), it is now stored.
			 */
			return this.registerAdd(instance);
		}

		@Override
		public final PersistenceObjectRetriever getObjectRetriever()
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
			this.internalInitialize(XHashing.padHashLength(initialCapacity));
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
			this.internalInitialize(XHashing.padHashLength(initialCapacity));
			return this;
		}

		private void internalInitialize()
		{
			this.internalInitialize(defaultSlotSize());
		}

		protected void internalInitialize(final int hashLength)
		{
			this.hashSlots = new Item[hashLength];
			this.hashRange = hashLength - 1;
			
			this.createStoringChunksBuffers();
		}
		
		private void createStoringChunksBuffers()
		{
			final ChunksBuffer[] chunks = this.chunks = new ChunksBuffer[this.chunksHashRange + 1];
			for(int i = 0; i < chunks.length; i++)
			{
				chunks[i] = this.switchByteOrder
					? ChunksBufferByteReversing.New(chunks, this.bufferSizeProvider)
					: ChunksBuffer.New(chunks, this.bufferSizeProvider)
				;
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
			return Long.MAX_VALUE;
		}

		@Override
		public PersistenceStorer<Binary> ensureCapacity(final long desiredCapacity)
		{
			if(this.currentCapacity() >= desiredCapacity)
			{
				return this;
			}
			this.rebuildStoreItems(XHashing.padHashLength(desiredCapacity));
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
			/* Note:
			 * may explicitly NOT clear (deallocate) the current chunks
			 * because in use with embedded (in-process) storage the chunks
			 * might still be used by the storage worker threads to update their entity caches.
			 * The released chunks must be handled by those threads if existing
			 * or ultimately by the garbage collector (or by some tailored additional logic)
			 */
			this.chunks = null;
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
			if(this.hashSlots.length >= XMath.highestPowerOf2_int())
			{
				return; // note that aborting rebuild does not ruin anything.
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
		
		@Override
		public final void accept(final long oid, final Object instance)
		{
			// ensure handler (or fail if type is not persistable) before ensuring an OID.
			this.tail = this.tail.next = this.registerOid(
				instance,
				this.typeManager.ensureTypeHandler(instance),
				oid
			);
		}
		
		protected final long registerAdd(final Object instance)
		{
			final long oid = this.objectManager.ensureObjectId(instance);
			this.accept(oid, instance);

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
			final PersistenceObjectRetriever            objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder
		)
		{
			super(
				objectManager     ,
				objectRetriever   ,
				typeManager       ,
				target            ,
				bufferSizeProvider,
				channelCount      ,
				switchByteOrder
			);
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
		
	public static BinaryStorer.Creator Creator(
		final _intReference channelCountProvider,
		final boolean       switchByteOrder
	)
	{
		return new BinaryStorer.Creator.Implementation(
			notNull(channelCountProvider),
			        switchByteOrder
		);
	}
		
	public interface Creator extends PersistenceStorer.Creator<Binary>
	{
		@Override
		public BinaryStorer createLazyStorer(
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceObjectManager              objectManager     ,
			PersistenceObjectRetriever            objectRetriever   ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProviderIncremental         bufferSizeProvider
		);
		
		@Override
		public default BinaryStorer createStorer(
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceObjectManager              objectManager     ,
			final PersistenceObjectRetriever            objectRetriever   ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider
		)
		{
			return this.createLazyStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider);
		}
		
		@Override
		public BinaryStorer createEagerStorer(
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceObjectManager              objectManager     ,
			PersistenceObjectRetriever            objectRetriever   ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProviderIncremental         bufferSizeProvider
		);
		
		
		
		public abstract class AbstractImplementation implements BinaryStorer.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields  //
			/////////////////////


			private final _intReference channelCountProvider;
			private final boolean       switchByteOrder     ;



			///////////////////////////////////////////////////////////////////////////
			// constructors     //
			/////////////////////

			protected AbstractImplementation(
				final _intReference channelCountProvider,
				final boolean       switchByteOrder
			)
			{
				super();
				this.channelCountProvider = channelCountProvider;
				this.switchByteOrder      = switchByteOrder     ;
			}

			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			protected int channelCount()
			{
				return this.channelCountProvider.get();
			}
			
			protected boolean switchByteOrder()
			{
				return this.switchByteOrder;
			}

		}
		
		public final class Implementation extends AbstractImplementation
		{
			Implementation(
				final _intReference channelCountProvider,
				final boolean       switchByteOrder
			)
			{
				super(channelCountProvider, switchByteOrder);
			}

			@Override
			public final BinaryStorer createLazyStorer(
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceObjectManager              objectManager     ,
				final PersistenceObjectRetriever            objectRetriever   ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider
			)
			{
				return new BinaryStorer.Implementation(
					objectManager         ,
					objectRetriever       ,
					typeManager           ,
					target                ,
					bufferSizeProvider    ,
					this.channelCount()   ,
					this.switchByteOrder()
				);
			}
			@Override
			public BinaryStorer createEagerStorer(
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceObjectManager              objectManager     ,
				final PersistenceObjectRetriever            objectRetriever   ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider
			)
			{
				return new BinaryStorer.ImplementationEager(
					objectManager         ,
					objectRetriever       ,
					typeManager           ,
					target                ,
					bufferSizeProvider    ,
					this.channelCount()   ,
					this.switchByteOrder()
				);
			}

		}
				
	}

}
